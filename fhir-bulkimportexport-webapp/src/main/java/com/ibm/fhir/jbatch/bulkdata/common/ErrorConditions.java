/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.common;

/**
 * Error Conditions
 */
public enum ErrorConditions {
    CANNOT_CREATE_COS_CLIENT(1),
    NO_TENANT_SPECIFIED(2),
    NO_DATASTORE_SPECIFIED(3),
    UNABLE_TO_CREATE_REQUEST_CONTEXT(4),
    UNABLE_TO_GET_CONFIGURATION(5),
    TRANSACTION_FAILURE(6),
    DATASOURCE_TYPE_DOES_NOT_EXIST(7);


    private final long value;

    ErrorConditions(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    public static ErrorConditions from(String value) {
        long val = Long.parseLong(value);
        for (ErrorConditions c : ErrorConditions.values()) {
            if (c.value == val) {
                return c;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
