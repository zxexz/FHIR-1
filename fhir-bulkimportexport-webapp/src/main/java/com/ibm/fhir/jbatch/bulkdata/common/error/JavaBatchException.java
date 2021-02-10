/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.common.error;

import com.ibm.fhir.jbatch.bulkdata.common.ErrorConditions;

/**
 *
 */
public class JavaBatchException extends Exception {

    private static final long serialVersionUID = -4043855075110056640L;

    private ErrorConditions error = null;

    public JavaBatchException(ErrorConditions error) {
        this.error = error;
    }

    public ErrorConditions getErrorCondition() {
        return this.error;
    }
}
