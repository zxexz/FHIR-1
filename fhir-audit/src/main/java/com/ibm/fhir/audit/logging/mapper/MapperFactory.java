/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.audit.logging.mapper;

import com.ibm.fhir.audit.logging.mapper.impl.CADFMapper;
import com.ibm.fhir.audit.logging.mapper.impl.AuditEventMapper;

/**
 *
 */
public class MapperFactory {

    /**
     * gets the appropriate mapper
     *
     * @param mt
     * @return
     */
    public static Mapper getMapper(MapperType mt) {
        switch (mt) {
        case CADF:
            return new CADFMapper();
        case FHIR:
            return new AuditEventMapper();
        default:
            throw new IllegalArgumentException("the registered mapper does not exist");
        }
    }

}