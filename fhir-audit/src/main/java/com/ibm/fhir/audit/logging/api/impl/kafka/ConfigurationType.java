/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.audit.logging.api.impl.kafka;

import java.util.Objects;

import com.ibm.fhir.audit.logging.api.AuditLogServiceConstants;
import com.ibm.fhir.config.PropertyGroup;

/**
 * This class marks the possible types of where the Configuration is found:
 * 1 - config (within the fhir-server-config.json)
 * 2 - environment (as part of the ENVIRONMENT variables and the fhir-server-config.json)
 * 3 - UNKNOWN
 */
public enum ConfigurationType {

    CONFIG("config"),
    ENVIRONMENT("environment"),
    UNKNOWN("unknown");

    private String value;

    ConfigurationType(String value) {
        this.value = value;
    }

    public ConfigurationType from(String value) {
        for(ConfigurationType mt : ConfigurationType.values()) {
            if (mt.value.equals(value)) {
                return mt;
            }
        }
        // No idea who, and assign to unknown.
        return UNKNOWN;
    }

    public String value() {
        return value;
    }

    /**
     * gets the location to load the properties from:
     * 1 - Environment Bindings with EventStreams Credentials - "env"
     * 2 - fhir-server-config.json - "config"
     * @param auditLogProperties
     * @return
     */
    public static ConfigurationType loadFromEnvironment(PropertyGroup auditLogProperties) {
        Objects.requireNonNull(auditLogProperties, "Expected Non Null audit log properties");
        ConfigurationType type = ConfigurationType.CONFIG;
        try {
            // Wrapping the possibly exception with getStringProperty
            type = ConfigurationType.valueOf(auditLogProperties.getStringProperty(AuditLogServiceConstants.FIELD_LOAD, ConfigurationType.CONFIG.value()));
        } catch (Exception e) {
            type = ConfigurationType.UNKNOWN;
        }
        return type;
    }
}