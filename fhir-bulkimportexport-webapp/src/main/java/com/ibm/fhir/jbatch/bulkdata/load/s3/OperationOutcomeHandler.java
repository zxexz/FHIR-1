/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.s3;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.COS_PART_MINIMALSIZE;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;

import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.load.ImportTransientUserData;

/**
 *
 */
public class OperationOutcomeHandler {

    public OperationOutcomeHandler() {
        //NO Operation
    }

    /*
     * Pushes the OperationOutcomes to COS
     */
    private void pushImportOperationOutcomesToCOS(ImportTransientUserData chunkData, String cosOperationOutcomesBucketName) throws Exception{
        // Upload OperationOutcomes in buffer if it reaches the minimal size for multiple-parts upload.
        if (chunkData.getBufferStreamForImport().size() > COS_PART_MINIMALSIZE) {
            if (chunkData.getUploadIdForOperationOutcomes() == null) {
                chunkData.setUploadIdForOperationOutcomes(BulkDataUtils.startPartUpload(s3wrapper.getClient(),
                        cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportOperationOutcomes(), true));
            }

            chunkData.getDataPacksForOperationOutcomes().add(BulkDataUtils.multiPartUpload(s3wrapper.getClient(),
                    cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportOperationOutcomes(),
                    chunkData.getUploadIdForOperationOutcomes(), new ByteArrayInputStream(chunkData.getBufferStreamForImport().toByteArray()),
                    chunkData.getBufferStreamForImport().size(), chunkData.getPartNumForOperationOutcomes()));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("pushImportOperationOutcomesToCOS: '" + chunkData.getBufferStreamForImport().size()
                    + "' bytes were successfully appended to COS object - " + chunkData.getUniqueIDForImportOperationOutcomes());
            }
            chunkData.setPartNumForOperationOutcomes(chunkData.getPartNumForOperationOutcomes() + 1);
            chunkData.getBufferStreamForImport().reset();
        }

        // Upload OperationOutcomes in failure buffer if it reaches the minimal size for multiple-parts upload.
        if (chunkData.getBufferStreamForImportError().size() > COS_PART_MINIMALSIZE) {
            if (chunkData.getUploadIdForFailureOperationOutcomes()  == null) {
                chunkData.setUploadIdForFailureOperationOutcomes(BulkDataUtils.startPartUpload(s3wrapper.getClient(),
                        cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportFailureOperationOutcomes(), true));
            }

            chunkData.getDataPacksForFailureOperationOutcomes().add(BulkDataUtils.multiPartUpload(s3wrapper.getClient(),
                    cosOperationOutcomesBucketName, chunkData.getUniqueIDForImportFailureOperationOutcomes(),
                    chunkData.getUploadIdForFailureOperationOutcomes(), new ByteArrayInputStream(chunkData.getBufferStreamForImportError().toByteArray()),
                    chunkData.getBufferStreamForImportError().size(), chunkData.getPartNumForFailureOperationOutcomes()));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("pushImportOperationOutcomesToCOS: '" + chunkData.getBufferStreamForImportError().size()
                    + "' bytes were successfully appended to COS object - " + chunkData.getUniqueIDForImportFailureOperationOutcomes());
            }
            chunkData.setPartNumForFailureOperationOutcomes(chunkData.getPartNumForFailureOperationOutcomes() + 1);
            chunkData.getBufferStreamForImportError().reset();
        }
    }


}
