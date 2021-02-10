/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.source;

import java.util.logging.Logger;

import com.ibm.fhir.jbatch.bulkdata.common.StorageType;
import com.ibm.fhir.jbatch.bulkdata.common.error.JavaBatchException;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;

/**
 *
 */
public class ImportSourceFactory {
    private static final Logger logger = Logger.getLogger(ImportSourceFactory.class.getName());

    private ImportSourceFactory() {
        // No Operation and intentionally hidden
    }

    public static Source getSource(String type, String dataUrl, long skipLines, ImportTransientUserData chunkData) {
        Source source = null;
        switch (StorageType.from(type)) {
        case HTTPS:
            source = new HttpsSource();

            break;
        case FILE:
            source = new FileSource();


            break;
        case AWSS3:
        case IBMCOS:

            break;
        default:
            logger.warning("readItem: Data source storage type not found!");
            throw new JavaBatchException(ErrorCondition.DATASOURCE_TYPE_DOES_NOT_EXIST);
            break;
        }
        return source;
    }
}
