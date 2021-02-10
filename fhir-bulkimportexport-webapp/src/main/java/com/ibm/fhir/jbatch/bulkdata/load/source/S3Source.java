/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectInputStream;
import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.common.Constants;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.resource.Resource;

/**
 *
 */
public class S3Source extends AbstractSource {
    public S3Source () {
        // No Operation
    }

    @Override
    public void read(String dataUrl, long lines, ImportTransientUserData chunkData) {
        numOfParseFailures =
                BulkDataUtils.readFhirResourceFromObjectStore(wrapper.getClient(), cosBucketName, importPartitionWorkitem, skipLines, chunkData);

    }


    /**
     * @param cosClient - COS/S3 client.
     * @param bucketName - COS/S3 bucket name to read from.
     * @param itemName - COS/S3 object name to read from.
     * @param numOfLinesToSkip - number of lines to skip before read.
     * @param fhirResources - List holds the FHIR resources.
     * @param transientUserData - transient user data for the chunk.
     * @return - number of parsing failures.
     * @throws Exception
     */
    public static int readFhirResourceFromObjectStore(AmazonS3 cosClient, String bucketName, String itemName,
           int numOfLinesToSkip, List<Resource> fhirResources, ImportTransientUserData transientUserData) throws Exception {
        int parseFailures = 0;
        int retryTimes = Constants.IMPORT_RETRY_TIMES;
        do {
            try {
                if (transientUserData.getBufferReader() == null) {
                    S3Object item = cosClient.getObject(new GetObjectRequest(bucketName, itemName));
                    S3ObjectInputStream s3InStream = item.getObjectContent();
                    transientUserData.setInputStream(s3InStream);
                    BufferedReader resReader = new BufferedReader(new InputStreamReader(s3InStream));
                    transientUserData.setBufferReader(resReader);
                    // Skip the already processed lines after opening the input stream for first read.
                    parseFailures = getFhirResourceFromBufferReader(transientUserData.getBufferReader(), numOfLinesToSkip, fhirResources, true, itemName);
                } else {
                    parseFailures = getFhirResourceFromBufferReader(transientUserData.getBufferReader(), numOfLinesToSkip, fhirResources, false, itemName);
                }
                break;
            } catch (Exception ex) {
                // Prepare for retry, skip all the processed lines in previous batches and this batch.
                numOfLinesToSkip = numOfLinesToSkip + fhirResources.size() + parseFailures;
                cleanupTransientUserData(transientUserData, true);
                logger.warning("readFhirResourceFromObjectStore: Error proccesing file [" + itemName + "] - " + ex.getMessage());
                if ((retryTimes--) > 0) {
                    logger.warning("readFhirResourceFromObjectStore: Retry ...");
                } else {
                    // Throw exception to fail the job, the job can be continued from the current checkpoint after the problem is solved.
                    throw ex;
                }
            }
        } while (retryTimes > 0);

        return parseFailures;
    }


    public static long getCosFileSize(AmazonS3 cosClient, String bucketName, String itemName) throws Exception {
            S3Object item = cosClient.getObject(new GetObjectRequest(bucketName, itemName));
            return item.getObjectMetadata().getContentLength();
      }
}
