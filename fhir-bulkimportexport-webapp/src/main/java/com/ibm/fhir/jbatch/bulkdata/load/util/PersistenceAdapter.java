/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.load.util;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.NDJSON_LINESEPERATOR;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.jbatch.bulkdata.common.ErrorConditions;
import com.ibm.fhir.jbatch.bulkdata.common.error.JavaBatchException;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.persistence.FHIRPersistence;
import com.ibm.fhir.persistence.context.FHIRPersistenceContext;
import com.ibm.fhir.persistence.context.FHIRPersistenceContextFactory;
import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.helper.FHIRPersistenceHelper;
import com.ibm.fhir.persistence.helper.FHIRTransactionHelper;

/**
 * Wraps the Persistence
 */
public class PersistenceAdapter {

    private static final Logger logger = Logger.getLogger(PersistenceAdapter.class.getName());

    private int processedNum = 0;
    private int succeededNum = 0;
    private boolean collectImportOperationOutcomes = false;
    private ImportTransientUserData chunkData = null;

    private FHIRTransactionHelper txn = null;
    private FHIRPersistence fhirPersistence = null;
    private FHIRPersistenceContext persistenceContext = null;
    private long writeStartTimeInMilliSeconds = System.currentTimeMillis();

    public PersistenceAdapter(boolean collectImportOperationOutcomes, ImportTransientUserData chunkData) {
        this.chunkData = chunkData;
        this.collectImportOperationOutcomes = collectImportOperationOutcomes;
    }

    public void begin() throws JavaBatchException {
        try {
            FHIRPersistenceHelper fhirPersistenceHelper = new FHIRPersistenceHelper();
            fhirPersistence = fhirPersistenceHelper.getFHIRPersistenceImplementation();
            persistenceContext = FHIRPersistenceContextFactory.createPersistenceContext(null);

            txn = new FHIRTransactionHelper(fhirPersistence.getTransaction());

            // Begin writing the resources into DB.

            // Acquire a DB connection which will be used in the batch.
            // This doesn't really start the transaction, because the transaction has already been started by the
            // JavaBatch
            // framework at this time point.
            txn.begin();
        } catch (FHIRPersistenceException e) {
            logger.warning("Unable to begin the transaction");
            throw new JavaBatchException(ErrorConditions.TRANSACTION_FAILURE);
        }
    }

    public void end() throws JavaBatchException {
        try {
            // Release the DB connection.

            // This doesn't really commit the transaction, because the transaction was started and will be committed
            // by the JavaBatch framework.
            txn.end();
        } catch (FHIRPersistenceException e) {
            logger.warning("Unable to close the transaction");
            throw new JavaBatchException(ErrorConditions.TRANSACTION_FAILURE);
        }
    }

    public void persist(List<Resource> resources) {
        try {
            for (Resource fhirResource : resources) {
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
        } catch (FHIRPersistenceException e) {
            logger.warning("Unable to process the data during the transaction");
            throw new JavaBatchException(ErrorConditions.TRANSACTION_FAILURE);
        }
    }

    public void updateResults() {
        chunkData.setTotalWriteMilliSeconds(chunkData.getTotalWriteMilliSeconds() + (System.currentTimeMillis() - writeStartTimeInMilliSeconds));
        chunkData.setNumOfProcessedResources(chunkData.getNumOfProcessedResources() + processedNum + chunkData.getNumOfParseFailures());
        chunkData.setNumOfImportedResources(chunkData.getNumOfImportedResources() + succeededNum);
        chunkData.setNumOfImportFailures(chunkData.getNumOfImportFailures() + failedNum + chunkData.getNumOfParseFailures());
        // Reset NumOfParseFailures for next batch.
        chunkData.setNumOfParseFailures(0);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("writeItems: processed " + processedNum + " " + importPartitionResourceType + " from " + chunkData.getImportPartitionWorkitem());
        }
    }
}
