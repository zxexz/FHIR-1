/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.common;

import java.util.logging.Logger;

import com.ibm.fhir.config.FHIRConfigHelper;
import com.ibm.fhir.config.FHIRConfiguration;
import com.ibm.fhir.config.FHIRRequestContext;
import com.ibm.fhir.jbatch.bulkdata.common.error.JavaBatchException;

/**
 * Configuration Manager wraps the call to the configuration objects in order
 * to support the mutation to a JavaBatchException so we can flow out better errors.
 */
public class ConfigurationManager {

    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());

    public ConfigurationManager() {
        // No Operation
    }

    /**
     * verifies the input information and creates a request context
     *
     * @param tenantId
     * @param dataStoreId
     * @throws JavaBatchException
     */
    public void createRequestContext(String tenantId, String dataStoreId, String incomingUrl) throws JavaBatchException {
        if (tenantId == null) {
            logger.warning("createRequestContext: tenant is not specified");
            throw new JavaBatchException(ErrorConditions.NO_TENANT_SPECIFIED);
        }
        if (dataStoreId == null) {
            logger.info("open: Set DatastoreId to default!");
            throw new JavaBatchException(ErrorConditions.NO_DATASTORE_SPECIFIED);
        }

        try {
            // Create a new FHIRRequestContext and set it on the current thread.
            FHIRRequestContext context = new FHIRRequestContext(tenantId, dataStoreId);
            // Don't try using FHIRConfigHelper before setting the context!
            FHIRRequestContext.set(context);
            context.setOriginalRequestUri(incomingUrl);
            context.setBulk(true);
        } catch (Exception e) {
            logger.warning("Unable to create the request context");
            throw new JavaBatchException(ErrorConditions.NO_DATASTORE_SPECIFIED);
        }
    }

    /**
     * the default is to validate, and we double check the configuration
     *
     * @return
     * @throws JavaBatchException
     */
    public boolean shouldValidate() throws JavaBatchException {
        try {
            return !FHIRConfigHelper.getBooleanProperty(FHIRConfiguration.PROPERTY_BULKDATA_VALIDATE_IMPORT_RESOURCES, false);
        } catch (RuntimeException re) {
            logger.severe("Unable to get the configuration for the collect import and bulkdata");
            throw new JavaBatchException(ErrorConditions.UNABLE_TO_GET_CONFIGURATION);
        }
    }

    /**
     * check the configuration, the default is to collect outcomes
     *
     * @return
     * @throws JavaBatchException
     */
    public boolean shouldCollectImportOperationOutcomes() throws JavaBatchException {
        try {
            // Controls the writing of operation outcomes to S3/COS
            return !FHIRConfigHelper.getBooleanProperty(FHIRConfiguration.PROPERTY_BULKDATA_IGNORE_IMPORT_OPERATION_OUTCOMES, false);
        } catch (RuntimeException re) {
            logger.severe("Unable to get the configuration for the collect import and bulkdata");
            throw new JavaBatchException(ErrorConditions.UNABLE_TO_GET_CONFIGURATION);
        }
    }

    /**
     * checks if the IBM FHIR Server truststore should be used;
     *
     * @return
     * @throws JavaBatchException
     */
    public boolean shouldUseFHIRServerTrustStore() throws JavaBatchException {
        try {
            return FHIRConfigHelper.getBooleanProperty(FHIRConfiguration.PROPERTY_BULKDATA_BATCHJOB_USEFHIRSERVERTRUSTSTORE, false);
        } catch (RuntimeException re) {
            logger.severe("Unable to get the configuration for the fhir trust store and bulkdata");
            throw new JavaBatchException(ErrorConditions.UNABLE_TO_GET_CONFIGURATION);
        }
    }
}
