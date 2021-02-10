/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.source;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.resource.Resource;

/**
 *
 */
public class FileSource extends AbstractSource {
    public FileSource () {
        // No Operation
    }

    @Override
    public void read(String dataUrl, long lines, ImportTransientUserData chunkData) {
        numOfParseFailures = BulkDataUtils.readFhirResourceFromLocalFile(importPartitionWorkitem, skipLines, chunkData);
    }

    public static long getLocalFileSize(String filePath) throws Exception {
        return (new File(filePath).length());
      }

    /**
     * @param filePath - file path to the ndjson file.
     * @param numOfLinesToSkip - number of lines to skip before read.
     * @param fhirResources - List holds the FHIR resources.
     * @param transientUserData - transient user data for the chunk.
     * @return - number of parsing failures.
     * @throws Exception
     */
    public static int readFhirResourceFromLocalFile(String filePath, int numOfLinesToSkip, List<Resource> fhirResources,
            ImportTransientUserData transientUserData) throws Exception {
        int parseFailures = 0;

        try {
            if (transientUserData.getBufferReader() == null) {
                BufferedReader resReader = Files.newBufferedReader(Paths.get(filePath));
                transientUserData.setBufferReader(resReader);
                // Skip the already processed lines after opening the input stream for first read.
                parseFailures = getFhirResourceFromBufferReader(transientUserData.getBufferReader(), numOfLinesToSkip, fhirResources, true, filePath);
            } else {
                parseFailures = getFhirResourceFromBufferReader(transientUserData.getBufferReader(), numOfLinesToSkip, fhirResources, false, filePath);
            }
        } catch (Exception ex) {
            // Clean up.
            fhirResources.clear();
            cleanupTransientUserData(transientUserData, true);
            // Log the error and throw exception to fail the job, the job can be continued from the current checkpoint after the problem is solved.
            logger.warning("readFhirResourceFromLocalFile: Error proccesing file [" + filePath + "] - " + ex.getMessage());
            throw ex;
        }

        return parseFailures;
    }
}
