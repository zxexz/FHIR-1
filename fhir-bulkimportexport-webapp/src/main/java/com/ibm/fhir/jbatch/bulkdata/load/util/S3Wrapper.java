/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.util;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_PART_MINIMALSIZE;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;

/**
 *
 */
public class S3Wrapper {
    private static final Logger logger = Logger.getLogger(S3Wrapper.class.getName());
    private AmazonS3 client = null;

    private int numOfLinesToSkip = 0;

    public S3Wrapper() {
        // No Operation
    }

    public void startup(String cosCredentialIbm, String cosApiKeyProperty, String cosSrvinstId,
        String cosEndpointUrl, String cosLocation, boolean useTrustStore) {
        client =
                BulkDataUtils.getCosClient(cosCredentialIbm, cosApiKeyProperty, cosSrvinstId, cosEndpointUrl,
                    cosLocation, useTrustStore);

            if (client == null) {
                logger.warning("open: Failed to get CosClient!");
                throw new Exception("Failed to get CosClient!!");
            } else {
                logger.finer("open: Got CosClient successfully!");
            }
    }

    public AmazonS3 getClient() {
        return client;
    }

    /*
     * Pushes the Operation OUtcomes to COS
     */
    public void pushImportOperationOutcomesToCOS(ImportTransientUserData chunkData) throws Exception{
        // Upload OperationOutcomes in buffer if it reaches the minimal size for multiple-parts upload.
        if (chunkData.getBufferStreamForImport().size() > COS_PART_MINIMALSIZE) {
            if (chunkData.getUploadIdForOperationOutcomes() == null) {
                chunkData.setUploadIdForOperationOutcomes(BulkDataUtils.startPartUpload(cosClient,
                        cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportOperationOutcomes(), true));
            }

            chunkData.getDataPacksForOperationOutcomes().add(BulkDataUtils.multiPartUpload(cosClient,
                    cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportOperationOutcomes(),
                    chunkData.getUploadIdForOperationOutcomes(), new ByteArrayInputStream(chunkData.getBufferStreamForImport().toByteArray()),
                    chunkData.getBufferStreamForImport().size(), chunkData.getPartNumForOperationOutcomes()));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("pushImportOperationOutcomesToCOS: " + chunkData.getBufferStreamForImport().size()
                    + " bytes were successfully appended to COS object - " + chunkData.getUniqueIDForImportOperationOutcomes());
            }
            chunkData.setPartNumForOperationOutcomes(chunkData.getPartNumForOperationOutcomes() + 1);
            chunkData.getBufferStreamForImport().reset();
        }

        // Upload OperationOutcomes in failure buffer if it reaches the minimal size for multiple-parts upload.
        if (chunkData.getBufferStreamForImportError().size() > COS_PART_MINIMALSIZE) {
            if (chunkData.getUploadIdForFailureOperationOutcomes()  == null) {
                chunkData.setUploadIdForFailureOperationOutcomes(BulkDataUtils.startPartUpload(cosClient,
                        cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportFailureOperationOutcomes(), true));
            }

            chunkData.getDataPacksForFailureOperationOutcomes().add(BulkDataUtils.multiPartUpload(cosClient,
                    cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportFailureOperationOutcomes(),
                    chunkData.getUploadIdForFailureOperationOutcomes(), new ByteArrayInputStream(chunkData.getBufferStreamForImportError().toByteArray()),
                    chunkData.getBufferStreamForImportError().size(), chunkData.getPartNumForFailureOperationOutcomes()));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("pushImportOperationOutcomesToCOS: " + chunkData.getBufferStreamForImportError().size()
                    + " bytes were successfully appended to COS object - " + chunkData.getUniqueIDForImportFailureOperationOutcomes());
            }
            chunkData.setPartNumForFailureOperationOutcomes(chunkData.getPartNumForFailureOperationOutcomes() + 1);
            chunkData.getBufferStreamForImportError().reset();
        }
    }

}
