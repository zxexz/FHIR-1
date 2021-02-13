/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.load;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_LOCATION;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.FHIR_DATASTORE_ID;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.FHIR_TENANT;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.INCOMING_URL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.NDJSON_LINESEPERATOR;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.PARTITION_RESOURCE_TYPE;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.SOURCE;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.runtime.context.StepContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.jbatch.bulkdata.audit.BulkAuditLogger;
import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.common.S3Wrapper;
import com.ibm.fhir.jbatch.bulkdata.configuration.ConfigurationManager;
import com.ibm.fhir.jbatch.bulkdata.load.s3.OperationOutcomeHandler;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.persistence.FHIRPersistence;
import com.ibm.fhir.persistence.context.FHIRPersistenceContext;
import com.ibm.fhir.persistence.context.FHIRPersistenceContextFactory;
import com.ibm.fhir.persistence.helper.FHIRPersistenceHelper;
import com.ibm.fhir.persistence.helper.FHIRTransactionHelper;
import com.ibm.fhir.validation.exception.FHIRValidationException;

/**
 * Bulk import Chunk implementation - the Writer.
 *
 */
@Dependent
public class ChunkWriter extends AbstractItemWriter {
    private static final Logger logger = Logger.getLogger(ChunkWriter.class.getName());

    private ConfigurationManager config = ConfigurationManager.getTranslator();
    private OperationOutcomeHandler ooHandler = new OperationOutcomeHandler();

    private static final BulkAuditLogger auditLogger = new BulkAuditLogger();

    private S3Wrapper s3wrapper = new S3Wrapper();

    @Inject
    StepContext stepCtx;

    @Inject
    @BatchProperty(name = FHIR_TENANT)
    String tenantId;

    @Inject
    @BatchProperty(name = FHIR_DATASTORE_ID)
    String datastoreId;

    // The IBM COS or S3 location.
    @Inject
    @BatchProperty(name = COS_LOCATION)
    String cosLocation;

    // Resource Type to process
    // Split from the original Parameters object posted to the server
    @Inject
    @BatchProperty(name = PARTITION_RESOURCE_TYPE)
    String importPartitionResourceType;

    @Inject
    @BatchProperty(name = INCOMING_URL)
    String incomingUrl;

    @Inject
    @BatchProperty(name = SOURCE)
    String source;

    public ChunkWriter() {
        super();
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        // No Operation
    }

    @Override
    public void writeItems(List<java.lang.Object> arg0) throws Exception {
        config.createImportRequestContext(tenantId, datastoreId, incomingUrl);

        if (config.shouldCollectImportOperationOutcomes()) {
            s3wrapper.initialize(source);
        }

        Set<String> failValidationIds = new HashSet<>();

        FHIRPersistenceHelper fhirPersistenceHelper = new FHIRPersistenceHelper();
        FHIRPersistence fhirPersistence = fhirPersistenceHelper.getFHIRPersistenceImplementation();
        FHIRPersistenceContext persistenceContext = FHIRPersistenceContextFactory.createPersistenceContext(null);

        FHIRTransactionHelper txn = new FHIRTransactionHelper(fhirPersistence.getTransaction());

        // Create a new FHIRRequestContext and set it on the current thread.
        config.createImportRequestContext(tenantId, datastoreId, incomingUrl);

        ImportTransientUserData chunkData = (ImportTransientUserData) stepCtx.getTransientUserData();

        // Controls the writing of operation outcomes to S3/COS
        boolean collectImportOperationOutcomes = config.shouldCollectImportOperationOutcomes();

        // Validate the resources first if required.
        int processedNum = 0, succeededNum =0, failedNum = 0;
        if (ConfigurationManager.getTranslator()
                .shouldValidateImportResources()) {
            long validationStartTimeInMilliSeconds = System.currentTimeMillis();
            for (Object objResJsonList : arg0) {
                @SuppressWarnings("unchecked")
                List<Resource> fhirResourceList = (List<Resource>) objResJsonList;

                for (Resource fhirResource : fhirResourceList) {
                    try {
                        BulkDataUtils.validateInput(fhirResource);
                    } catch (FHIRValidationException|FHIROperationException e) {
                        logger.warning("Failed to validate '" + fhirResource.getId() + "' due to error: " + e.getMessage());
                        failedNum++;
                        failValidationIds.add(fhirResource.getId());

                        if (collectImportOperationOutcomes) {
                            OperationOutcome operationOutCome = FHIRUtil.buildOperationOutcome(e, false);
                            FHIRGenerator.generator(Format.JSON).generate(operationOutCome, chunkData.getBufferStreamForImportError());
                            chunkData.getBufferStreamForImportError().write(NDJSON_LINESEPERATOR);
                        }
                    }
                }
            }
            chunkData.setTotalValidationMilliSeconds(chunkData.getTotalValidationMilliSeconds()
                    + (System.currentTimeMillis() - validationStartTimeInMilliSeconds));
        }

        // Begin writing the resources into DB.
        long writeStartTimeInMilliSeconds = System.currentTimeMillis();
        // Acquire a DB connection which will be used in the batch.
        // This doesn't really start the transaction, because the transaction has already been started by the JavaBatch
        // framework at this time point.
        txn.begin();

        try {
            for (Object objResJsonList : arg0) {
                @SuppressWarnings("unchecked")
                List<Resource> fhirResourceList = (List<Resource>) objResJsonList;

                for (Resource fhirResource : fhirResourceList) {
                    try {
                        String id = fhirResource.getId();
                        processedNum++;
                        // Skip the resources which failed the validation
                        if (failValidationIds.contains(id)) {
                            continue;
                        }
                        OperationOutcome operationOutcome;
                        if (id == null) {
                            operationOutcome =
                                    fhirPersistence.create(persistenceContext, fhirResource).getOutcome();
                        } else {
                            operationOutcome =
                                    fhirPersistence.update(persistenceContext, id, fhirResource).getOutcome();
                        }

                        succeededNum++;
                        if (collectImportOperationOutcomes && operationOutcome != null) {
                            FHIRGenerator.generator(Format.JSON).generate(operationOutcome, chunkData.getBufferStreamForImport());
                            chunkData.getBufferStreamForImport().write(NDJSON_LINESEPERATOR);
                        }
                    } catch (FHIROperationException e) {
                        logger.warning("Failed to import '" + fhirResource.getId() + "' due to error: " + e.getMessage());
                        failedNum++;
                        if (collectImportOperationOutcomes) {
                            OperationOutcome operationOutCome = FHIRUtil.buildOperationOutcome(e, false);
                            FHIRGenerator.generator(Format.JSON).generate(operationOutCome, chunkData.getBufferStreamForImportError());
                            chunkData.getBufferStreamForImportError().write(NDJSON_LINESEPERATOR);
                        }
                    }
                }
            }
        } finally {
            // Release the DB connection.
            // This doesn't really commit the transaction, because the transaction was started and will be committed
            // by the JavaBatch framework.
            txn.end();
        }

        chunkData.setTotalWriteMilliSeconds(chunkData.getTotalWriteMilliSeconds() + (System.currentTimeMillis() - writeStartTimeInMilliSeconds));
        chunkData.setNumOfProcessedResources(chunkData.getNumOfProcessedResources() + processedNum + chunkData.getNumOfParseFailures());
        chunkData.setNumOfImportedResources(chunkData.getNumOfImportedResources() + succeededNum);
        chunkData.setNumOfImportFailures(chunkData.getNumOfImportFailures() + failedNum + chunkData.getNumOfParseFailures());
        // Reset NumOfParseFailures for next batch.
        chunkData.setNumOfParseFailures(0);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("writeItems: processed " + processedNum + " " + importPartitionResourceType + " from " +  chunkData.getImportPartitionWorkitem());
        }

        // If The OperationOutcomes need to be collected, let's upload them to COS.
        if (collectImportOperationOutcomes) {
            ooHandler.pushImportOperationOutcomesToCOS(chunkData, config.getCosOperationOutcomesBucketName(source));
        }
    }
}