/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.source;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.common.Constants;
import com.ibm.fhir.jbatch.bulkdata.common.error.JavaBatchException;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.resource.Resource;

/**
 *
 */
public class HttpsSource extends AbstractSource {
    public HttpsSource () {
        // No Operation
    }

    public long getSize() throws JavaBatchException {
        HttpsURLConnection httpsConnection = null;
        try {
            // Check before trying to use 'http://' with an 'https://' url connection.
            if (dataUrl.startsWith("http://")) {
                throw new FHIROperationException("No support for 'http'");
            }

            httpsConnection = (HttpsURLConnection) new URL(dataUrl).openConnection();
            httpsConnection.setRequestMethod("HEAD");
            return httpsConnection.getContentLengthLong();
        } finally {
          if (httpsConnection != null) {
              httpsConnection.disconnect();
          }
        }
      }



    /**
     * @param dataUrl - URL to the ndjson file.
     * @param numOfLinesToSkip - number of lines to skip before read.
     * @param fhirResources - List holds the FHIR resources.
     * @param transientUserData - transient user data for the chunk.
     * @return - number of parsing failures.
     * @throws Exception
     */
    public int readFhirResourceFromHttps(String dataUrl, int numOfLinesToSkip, List<Resource> fhirResources,
            ImportTransientUserData transientUserData) throws Exception {
        int parseFailures = 0;
        int retryTimes = Constants.IMPORT_RETRY_TIMES;
        do {
            try {
                if (transientUserData.getBufferReader() == null) {
                    InputStream inputStream = new URL(dataUrl).openConnection().getInputStream();
                    transientUserData.setInputStream(inputStream);
                    BufferedReader resReader = new BufferedReader(new InputStreamReader(inputStream));
                    transientUserData.setBufferReader(resReader);
                    // Skip the already processed lines after opening the input stream for first read.
                    parseFailures = getFhirResourceFromBufferReader(transientUserData.getBufferReader(), numOfLinesToSkip, fhirResources, true, dataUrl);
                } else {
                    parseFailures = getFhirResourceFromBufferReader(transientUserData.getBufferReader(), numOfLinesToSkip, fhirResources, false, dataUrl);
                }
                break;
            } catch (Exception ex) {
                // Prepare for retry, skip all the processed lines in previous batches and this batch.
                numOfLinesToSkip = numOfLinesToSkip + fhirResources.size() + parseFailures;
                cleanupTransientUserData(transientUserData, true);
                logger.warning("readFhirResourceFromHttps: Error proccesing file [" + dataUrl + "] - " + ex.getMessage());
                if ((retryTimes--) > 0) {
                    logger.warning("readFhirResourceFromHttps: Retry ...");
                } else {
                    // Throw exception to fail the job, the job can be continued from the current checkpoint after the problem is solved.
                    throw ex;
                }
            }
        } while (retryTimes > 0);

        return parseFailures;
    }

    @Override
    public void read(String dataUrl, long lines, ImportTransientUserData chunkData) {
        BulkDataUtils.readFhirResourceFromHttps(importPartitionWorkitem, skipLines, chunkData);

    }
}
