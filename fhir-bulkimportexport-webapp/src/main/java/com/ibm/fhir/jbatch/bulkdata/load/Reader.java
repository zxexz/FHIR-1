/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.load;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_API_KEY;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_BUCKET_NAME;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_ENDPOINT_URL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_IS_IBM_CREDENTIAL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_LOCATION;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_SRVINST_ID;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.FHIR_DATASTORE_ID;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.FHIR_TENANT;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.IMPORT_FHIR_STORAGE_TYPE;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.IMPORT_PARTITTION_WORKITEM;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.PARTITION_RESOURCE_TYPE;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.StepContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.common.ConfigurationManager;
import com.ibm.fhir.jbatch.bulkdata.common.StorageType;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportCheckPointData;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.jbatch.bulkdata.load.source.ImportSourceFactory;
import com.ibm.fhir.jbatch.bulkdata.load.source.Source;
import com.ibm.fhir.jbatch.bulkdata.load.util.S3Wrapper;
import com.ibm.fhir.model.resource.Resource;

/**
 * BulkData Import Reader
 */
@Dependent
public class Reader extends AbstractItemReader {

    private static final Logger logger = Logger.getLogger(Reader.class.getName());

    @Inject
    StepContext stepCtx;

    /**
     * The data source storage type.
     */
    @Inject
    @BatchProperty(name = IMPORT_FHIR_STORAGE_TYPE)
    String dataSourceStorageType;

    /**
     * The IBM COS API key or S3 access key.
     */
    @Inject
    @BatchProperty(name = COS_API_KEY)
    String cosApiKeyProperty;

    /**
     * The IBM COS service instance id or S3 secret key.
     */
    @Inject
    @BatchProperty(name = COS_SRVINST_ID)
    String cosSrvinstId;

    /**
     * The IBM COS or S3 End point URL.
     */
    @Inject
    @BatchProperty(name = COS_ENDPOINT_URL)
    String cosEndpointUrl;

    /**
     * The IBM COS or S3 location.
     */
    @Inject
    @BatchProperty(name = COS_LOCATION)
    String cosLocation;

    /**
     * The IBM COS or S3 bucket name to import from.
     */
    @Inject
    @BatchProperty(name = COS_BUCKET_NAME)
    String cosBucketName;

    /**
     * If use IBM credential or S3 secret keys.
     */
    @Inject
    @BatchProperty(name = COS_IS_IBM_CREDENTIAL)
    String cosCredentialIbm;

    /**
     * Work item to process.
     */
    @Inject
    @BatchProperty(name = IMPORT_PARTITTION_WORKITEM)
    String importPartitionWorkitem;

    /**
     * Resource type to process.
     */
    @Inject
    @BatchProperty(name = PARTITION_RESOURCE_TYPE)
    String importPartitionResourceType;

    /**
     * Tenant id.
     */
    @Inject
    @BatchProperty(name = FHIR_TENANT)
    String tenantId;

    /**
     * Data store id.
     */
    @Inject
    @BatchProperty(name = FHIR_DATASTORE_ID)
    String dataStoreId;

    private ConfigurationManager manager = new ConfigurationManager();
    private S3Wrapper wrapper = new S3Wrapper();

    public Reader() {
        super();
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        // Only if we're dealing with S3/COS do we want to start the S3 Client
        if (StorageType.AWSS3.equals(StorageType.from(dataSourceStorageType))
                || StorageType.IBMCOS.equals(StorageType.from(dataSourceStorageType))) {
            manager.createRequestContext(tenantId, dataStoreId, null);
            wrapper.startup(cosCredentialIbm, cosApiKeyProperty, cosSrvinstId, cosEndpointUrl,
                cosLocation, manager.shouldUseFHIRServerTrustStore());
        } else if (logger.isLoggable(Level.FINE)){
            logger.fine("Storage Type of the Export is '" + dataSourceStorageType + "'");
        }

        // We're resuming a checkpoint.
        if (checkpoint != null) {
            ImportCheckPointData checkPointData = (ImportCheckPointData) checkpoint;
            importPartitionWorkitem = checkPointData.getImportPartitionWorkitem();
            checkPointData.setInFlyRateBeginMilliSeconds(System.currentTimeMillis());
            stepCtx.setTransientUserData(ImportTransientUserData.fromImportCheckPointData(checkPointData));
        } else {
            // New Import
            ImportTransientUserData chunkData =
                    (ImportTransientUserData) ImportTransientUserData.Builder.builder()
                        .importPartitionWorkitem(importPartitionWorkitem)
                        .numOfProcessedResources(0) // We're starting at zero.
                        .importPartitionResourceType(importPartitionResourceType)
                        // This naming pattern is used in bulkdata operation to generate file links for import
                        // OperationOutcomes.
                        // e.g, for input file test1.ndjson, if there is any error during the importing, then the errors
                        // are in
                        // test1.ndjson_oo_errors.ndjson
                        // Note: for those good imports, we don't really generate any meaningful OperationOutcome, so
                        // only error import
                        // OperationOutcomes are supported for now.
                        .uniqueIDForImportOperationOutcomes(importPartitionWorkitem + "_oo_success.ndjson")
                        .uniqueIDForImportFailureOperationOutcomes(importPartitionWorkitem + "_oo_errors.ndjson")
                        .build();
            long importFileSize = 0;
            switch (StorageType.from(dataSourceStorageType)) {
            case HTTPS:
                importFileSize = BulkDataUtils.getHttpsFileSize(importPartitionWorkitem);
                break;
            case FILE:
                importFileSize = BulkDataUtils.getLocalFileSize(importPartitionWorkitem);
                break;
            case AWSS3:
            case IBMCOS:
                importFileSize = BulkDataUtils.getCosFileSize(wrapper.getClient(), cosBucketName, importPartitionWorkitem);
                break;
            default:
                throw new IllegalStateException("Doesn't support data source storage type '" + dataSourceStorageType + "'!");
            }
            chunkData.setImportFileSize(importFileSize);
            chunkData.setInFlyRateBeginMilliSeconds(System.currentTimeMillis());
            stepCtx.setTransientUserData(chunkData);
        }
    }

    @Override
    public Object readItem() throws Exception {
        // If the job is being stopped or in other status except for "started", then stop the read.
        if (!BatchStatus.STARTED.equals(stepCtx.getBatchStatus())) {
            return null;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("readItem: get work item: '" + importPartitionWorkitem + "' resource type: " + importPartitionResourceType);
        }

        ImportTransientUserData chunkData = (ImportTransientUserData) stepCtx.getTransientUserData();

        int numOfLinesToSkip = chunkData.getNumOfProcessedResources();

        long readStartTimeInMilliSeconds = System.currentTimeMillis();
        int numOfLoaded = 0;
        int numOfParseFailures = 0;


        Source source = ImportSourceFactory.getSource(dataSourceStorageType, importPartitionWorkitem, numOfLinesToSkip, chunkData);
        List<Resource> resources = source.getResources();

        chunkData.setTotalReadMilliSeconds(chunkData.getTotalReadMilliSeconds() + (System.currentTimeMillis() - readStartTimeInMilliSeconds));
        chunkData.setNumOfParseFailures(chunkData.getNumOfParseFailures() + numOfParseFailures);
        numOfLoaded = loadedResources.size();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("readItem: loaded " + numOfLoaded + " " + importPartitionResourceType + " from " + importPartitionWorkitem);
        }
        chunkData.setNumOfToBeImported(numOfLoaded);
        if (numOfLoaded == 0) {
            return null;
        } else {
            return loadedResources;
        }
    }

    @Override
    public void close() throws Exception {
        // No Operation
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return ImportCheckPointData.fromImportTransientUserData((ImportTransientUserData) stepCtx.getTransientUserData());
    }
}