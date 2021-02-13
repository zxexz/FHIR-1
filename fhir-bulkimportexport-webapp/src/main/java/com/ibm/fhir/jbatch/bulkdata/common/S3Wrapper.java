/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.common;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

/**
 *
 */
public class S3Wrapper {
    AmazonS3 cosClient = null;

    /**
     *
     */
    public void initialize(String source) {
        cosClient =
                BulkDataUtils.getCosClient(config.isIBMS3Credential(), config.getCosApiKey(source), cosSrvinstId, cosEndpointUrl,
                    cosLocation, config.shouldUseTrustStore());

            if (cosClient == null) {
                logger.warning("open: Failed to get CosClient!");
                throw new Exception("Failed to get CosClient!!");
            } else {
                logger.finer("open: Got CosClient successfully!");
            }
    }

    /**
     * @return
     */
    public AmazonS3 getClient() {
        // TODO Auto-generated method stub
        return null;
    }
}
