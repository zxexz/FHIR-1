/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.configuration;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_API_KEY;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_ENDPOINT_URL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_IS_IBM_CREDENTIAL;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_OPERATIONOUTCOMES_BUCKET_NAME;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_SRVINST_ID;

import java.util.logging.Logger;

import javax.batch.api.BatchProperty;
import javax.inject.Inject;

import com.ibm.fhir.config.FHIRConfigHelper;
import com.ibm.fhir.config.FHIRConfiguration;
import com.ibm.fhir.config.FHIRRequestContext;
import com.ibm.fhir.exception.FHIRException;

/**
 *
 */
public class ConfigurationManager {
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
    private static final ConfigurationManager translator = new ConfigurationManager();

    private ConfigurationManager() {
        // No Operation
    }

    public static ConfigurationManager getTranslator() {
        return translator;
    }

    public void createExportRequestContext(String tenantId, String datastoreId) throws FHIRException {
        verifyTenantDataStore(tenantId, datastoreId);
        FHIRRequestContext context = new FHIRRequestContext(tenantId, datastoreId);
        FHIRRequestContext.set(context);
        context.setBulk(true);
    }

    public void createImportRequestContext(String tenantId, String datastoreId, String incomingUrl) throws FHIRException {
        verifyTenantDataStore(tenantId, datastoreId);
        FHIRRequestContext context = new FHIRRequestContext(tenantId, datastoreId);
        FHIRRequestContext.set(context);
        context.setOriginalRequestUri(incomingUrl);
        context.setBulk(true);
    }

    private void verifyTenantDataStore(String tenantId, String datastoreId) {
        if (tenantId == null) {
            logger.warning("tenantId passed to job is null");
        }
        if (datastoreId == null) {
            logger.info("datastoreId passed to job is null");
        }
    }

    public boolean shouldValidateImportResources() {
        return !FHIRConfigHelper
                .getBooleanProperty(FHIRConfiguration.PROPERTY_BULKDATA_IMPORT_VALIDATE_RESOURCE, false);
    }

    public boolean shouldUseTrustStore() {
        return FHIRConfigHelper
                .getBooleanProperty(FHIRConfiguration.PROPERTY_BULKDATA_BATCHJOB_USEFHIRSERVERTRUSTSTORE, false);
    }

    public boolean shouldCollectImportOperationOutcomes() {
        return !FHIRConfigHelper
                .getBooleanProperty(FHIRConfiguration.PROPERTY_BULKDATA_IGNORE_IMPORT_OPERATION_OUTCOMES, false);
    }

    public boolean isIBMS3Credential() {
        /**
         * If use IBM credential or S3 secret keys.
         */
        @Inject
        @BatchProperty(name = COS_IS_IBM_CREDENTIAL)
        String cosCredentialIbm;
    }

    public String getApiKeyOrAccessKey(String source) {

        /**
         * The IBM COS API key or S3 access key.
         */
        @Inject
        @BatchProperty(name = COS_API_KEY)
        String cosApiKeyProperty;

    }

    public String getSecretKeyOrPassword(String source) {
        /**
         * The IBM COS service instance id or S3 secret key.
         */
        @Inject
        @BatchProperty(name = COS_SRVINST_ID)
        String cosSrvinstId;

        return null;
    }

    public String getCosEndpointUrl(String source) {
        /**
         * The IBM COS or S3 End point URL.
         */
        @Inject
        @BatchProperty(name = COS_ENDPOINT_URL)
        String cosEndpointUrl;

        return null;
    }

    public String getCosOperationOutcomesBucketName(String source) {
        /**
         * The IBM COS or S3 bucket name for import OperationOutcomes.
         */
        @Inject
        @BatchProperty(name = COS_OPERATIONOUTCOMES_BUCKET_NAME)
        String cosOperationOutcomesBucketName;
        return null;
    }
}
