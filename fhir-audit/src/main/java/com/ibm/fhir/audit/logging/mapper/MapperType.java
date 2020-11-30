/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.audit.logging.mapper;

import com.ibm.fhir.config.FHIRConfigHelper;
import com.ibm.fhir.config.FHIRConfiguration;
import com.ibm.fhir.config.PropertyGroup;

/**
 * The various output types that need to be mapped from the AuditLogEntry.
 */
public enum MapperType {
    CADF("cadf"),
    FHIR("fhir");

    private String value;

    MapperType(String value) {
        this.value = value;
    }

    public static MapperType from(String value) {
        for(MapperType mt : MapperType.values()) {
            if (mt.value.equals(value)) {
                return mt;
            }
        }
        throw new IllegalArgumentException("Failed to find the mapper type");
    }

    /**
     * converts from a PropertyGroup
     *
     * @param auditLogProperties
     * @return
     */
    public static MapperType getMapperType(PropertyGroup auditLogProperties) {
        return from(FHIRConfigHelper.getStringProperty(FHIRConfiguration.PROPERTY_AUDIT_SERVICE_MAPPER_TYPE, "cadf"));
    }
}