/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.load;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_API_KEY;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_ENDPOINT_URL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_IS_IBM_CREDENTIAL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_LOCATION;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_OPERATIONOUTCOMES_BUCKET_NAME;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_SRVINST_ID;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.FHIR_DATASTORE_ID;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.FHIR_TENANT;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.INCOMING_URL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.PARTITION_RESOURCE_TYPE;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.runtime.context.StepContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.ibm.fhir.jbatch.bulkdata.common.ConfigurationManager;
import com.ibm.fhir.jbatch.bulkdata.common.error.JavaBatchException;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportDTO;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.jbatch.bulkdata.load.util.PersistenceAdapter;
import com.ibm.fhir.jbatch.bulkdata.load.util.S3Wrapper;
import com.ibm.fhir.jbatch.bulkdata.load.util.Validation;
import com.ibm.fhir.model.resource.Resource;

/**
 * Bulk $import ChunkWriter
 *
 * Responsible for taking the objects read in the ChunkReader and
 * writing into the Persistence layer
 */
@Dependent
public class Writer extends AbstractItemWriter {

    private static final Logger logger = Logger.getLogger(Writer.class.getName());

    private static final ConfigurationManager configuration = new ConfigurationManager();
    private S3Wrapper wrapper = new S3Wrapper();

    @Inject
    StepContext stepCtx;

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
     * The IBM COS or S3 bucket name for import OperationOutcomes.
     */
    @Inject
    @BatchProperty(name = COS_OPERATIONOUTCOMES_BUCKET_NAME)
    String cosOperationOutcomesBucketName;

    /**
     * If use IBM credential or S3 secret keys.
     */
    @Inject
    @BatchProperty(name = COS_IS_IBM_CREDENTIAL)
    String cosCredentialIbm;

    /**
     * Tenant id.
     */
    @Inject
    @BatchProperty(name = FHIR_TENANT)
    String tenantId;

    /**
     * Fhir data store id.
     */
    @Inject
    @BatchProperty(name = FHIR_DATASTORE_ID)
    String dataStoreId;

    /**
     * Resource Type to process.
     */
    @Inject
    @BatchProperty(name = PARTITION_RESOURCE_TYPE)
    String importPartitionResourceType;

    @Inject
    @BatchProperty(name = INCOMING_URL)
    String incomingUrl;

    public Writer() {
        super();
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        // We have to create the RequestContext in order to get the right configuration.
        try {
            configuration.createRequestContext(tenantId, dataStoreId, incomingUrl);
            wrapper.startup(cosCredentialIbm, cosApiKeyProperty, cosSrvinstId, cosEndpointUrl, cosLocation, configuration.shouldUseFHIRServerTrustStore());
        } catch (JavaBatchException ex) {
            // Capture so we can send to the ERROR
            logger.warning("Error Condition reached '" + ex.getErrorCondition().name() + "' '" + ex.getErrorCondition().value() + "'");

            ImportTransientUserData chunkData = (ImportTransientUserData) stepCtx.getTransientUserData();
            chunkData.setConditions(ex.getErrorCondition());
            stepCtx.setExitStatus("" + ex.getErrorCondition().value());
        }
    }

    @Override
    public void writeItems(List<java.lang.Object> arg0) throws Exception {
        ImportTransientUserData chunkData = (ImportTransientUserData) stepCtx.getTransientUserData();

        // Check to see if we've hit a condition that causes us to skip moving forward.
        if (chunkData.getConditions() == null) {
            try {
                // Create the Request Context before we go get properties.
                configuration.createRequestContext(tenantId, dataStoreId, incomingUrl);

                // Identify the validation is needed for each Resource processed
                boolean validate = configuration.shouldValidate();

                // Controls the writing of operation outcomes to S3/COS
                boolean collectImportOperationOutcomes = configuration.shouldCollectImportOperationOutcomes();

                // Validate the resources first if required.
                Validation validation = new Validation(chunkData, collectImportOperationOutcomes);
                if (validate) {
                    for (Object objResJsonList : arg0) {
                        @SuppressWarnings("unchecked")
                        List<Resource> resources = (List<Resource>) objResJsonList;
                        ImportDTO dto = new ImportDTO(resources);
                        validation.validate(dto);
                    }
                    validation.updateTotalValidation();
                }

                PersistenceAdapter persistence = new PersistenceAdapter(chunkData);
                persistence.begin();
                try {
                    for (Object objResJsonList : arg0) {
                        @SuppressWarnings("unchecked")
                        List<Resource> resources = (List<Resource>) objResJsonList;
                        persistence.persist(resources);
                    }
                } finally {
                    persistence.end();
                }

                if (collectImportOperationOutcomes) {
                    wrapper.pushImportOperationOutcomesToCOS(chunkData);
                }
            } catch (JavaBatchException ex) {
                // Capture so we can send to the ERROR
                logger.warning("Error Condition reached '" + ex.getErrorCondition().name() + "' '" + ex.getErrorCondition().value() + "'");
                chunkData.setConditions(ex.getErrorCondition());
                stepCtx.setExitStatus("" + ex.getErrorCondition().value());
            }
        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine("Skipping ChunkWriter as there is an existing Error Condition");
        }
    }
}