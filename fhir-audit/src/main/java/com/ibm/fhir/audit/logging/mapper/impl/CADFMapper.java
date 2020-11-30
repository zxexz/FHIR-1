/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.audit.logging.mapper.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.ibm.fhir.audit.logging.beans.AuditLogEntry;
import com.ibm.fhir.audit.logging.beans.AuditLogEventType;
import com.ibm.fhir.audit.logging.beans.FHIRContext;
import com.ibm.fhir.audit.logging.mapper.Mapper;
import com.ibm.fhir.audit.model.cadf.CadfAttachment;
import com.ibm.fhir.audit.model.cadf.CadfCredential;
import com.ibm.fhir.audit.model.cadf.CadfEndpoint;
import com.ibm.fhir.audit.model.cadf.CadfEvent;
import com.ibm.fhir.audit.model.cadf.CadfGeolocation;
import com.ibm.fhir.audit.model.cadf.CadfResource;
import com.ibm.fhir.audit.model.cadf.enums.Action;
import com.ibm.fhir.audit.model.cadf.enums.Outcome;
import com.ibm.fhir.audit.model.cadf.enums.ResourceType;
import com.ibm.fhir.audit.model.common.EventType;
import com.ibm.fhir.config.PropertyGroup;

/**
 *
 */
public class CADFMapper implements Mapper {

    private static final Logger logger = java.util.logging.Logger.getLogger(CADFMapper.class.getName());
    private static final String CLASSNAME = CADFMapper.class.getName();
    private static final String PROPERTY_AUDIT_KAFKA_TOPIC = "auditTopic";
    private static final String PROPERTY_AUDIT_GEO_CITY = "geoCity";
    private static final String PROPERTY_AUDIT_GEO_STATE = "geoState";
    private static final String PROPERTY_AUDIT_GEO_COUNTRY = "geoCounty";

    private static final String DEFAULT_AUDIT_KAFKA_TOPIC = "FHIR_AUDIT";
    private static final String DEFAULT_AUDIT_GEO_CITY = "Dallas";
    private static final String DEFAULT_AUDIT_GEO_STATE = "TX";
    private static final String DEFAULT_AUDIT_GEO_COUNTRY = "US";

    private static String bootstrapServers = null;
    private static String apiKey = null;
    private static String auditTopic = DEFAULT_AUDIT_KAFKA_TOPIC;
    private static String geoCity = DEFAULT_AUDIT_GEO_CITY;
    private static String geoState = DEFAULT_AUDIT_GEO_STATE;
    private static String geoCountry = DEFAULT_AUDIT_GEO_COUNTRY;

    private static final Map<String, Action> FHIR_TO_CADF_ACTION_MAP = new HashMap<String, Action>() {

        private static final long serialVersionUID = 1L;
        {
            put(AuditLogEventType.FHIR_CREATE.value(), Action.create);
            put(AuditLogEventType.FHIR_READ.value(), Action.read);
            put(AuditLogEventType.FHIR_UPDATE.value(), Action.update);
            put(AuditLogEventType.FHIR_DELETE.value(), Action.delete);
        }
    };

    // Initialize CADF OBSERVER resource object once
    private static CadfResource observerRsrc =
            new CadfResource.Builder("fhir-server", ResourceType.compute_node).geolocation(new CadfGeolocation.Builder(geoCity, geoState, geoCountry, null).build()).name("FHIR Audit").host(System.getenv("HOSTNAME")).build();
    private CadfEvent event = null;

    @Override
    public Mapper map(AuditLogEntry entry) {
        final String METHODNAME = "map";
        logger.entering(CLASSNAME, METHODNAME);
        // Now, let's get the audit topic from FHIR config, if not found, then use the default topic
        PropertyGroup auditLogProperties = null;
        if (auditLogProperties != null) {
            try {
                geoCity = auditLogProperties.getStringProperty(PROPERTY_AUDIT_GEO_CITY, DEFAULT_AUDIT_GEO_CITY);

                geoState = auditLogProperties.getStringProperty(PROPERTY_AUDIT_GEO_STATE, DEFAULT_AUDIT_GEO_STATE);
                geoCountry = auditLogProperties.getStringProperty(PROPERTY_AUDIT_GEO_COUNTRY, DEFAULT_AUDIT_GEO_COUNTRY);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Outcome cadfEventOutCome;

        // Cadf does't log config, so skip
        if ((entry.getEventType() != AuditLogEventType.FHIR_CONFIGDATA.value()) && entry.getContext() != null
                && entry.getContext().getAction() != null && entry.getContext().getApiParameters() != null) {
            // Define resources
            CadfResource initiator =
                    new CadfResource.Builder(entry.getTenantId() + "@"
                            + entry.getComponentId(), ResourceType.compute_machine).geolocation(new CadfGeolocation.Builder(geoCity, geoState, geoCountry, null).build()).credential(new CadfCredential.Builder("user-"
                                    + entry.getUserName()).build()).host(entry.getComponentIp()).build();
            CadfResource target =
                    new CadfResource.Builder(entry.getContext().getData() == null || entry.getContext().getData().getId() == null
                            ? UUID.randomUUID().toString()
                            : entry.getContext().getData().getId(), ResourceType.data_database).geolocation(new CadfGeolocation.Builder(geoCity, geoState, geoCountry, null).build()).address(new CadfEndpoint(entry.getContext().getApiParameters().getRequest(), "", "")).build();

            FHIRContext fhirContext = new FHIRContext(entry.getContext());
            fhirContext.setClient_cert_cn(entry.getClientCertCn());
            fhirContext.setClient_cert_issuer_ou(entry.getClientCertIssuerOu());
            fhirContext.setEventType(entry.getEventType());
            fhirContext.setLocation(entry.getLocation());
            fhirContext.setDescription(entry.getDescription());

            if (entry.getContext().getEndTime() == null ||
                    entry.getContext().getStartTime().equalsIgnoreCase(entry.getContext().getEndTime())) {
                cadfEventOutCome = Outcome.pending;
            } else if (entry.getContext().getApiParameters().getStatus() < 400) {
                cadfEventOutCome = Outcome.success;
            } else {
                cadfEventOutCome = Outcome.failure;
            }

            try {
                event = new CadfEvent.Builder(entry.getContext().getRequestUniqueId() == null ? UUID.randomUUID().toString()
                        : entry.getContext().getRequestUniqueId(), EventType.activity, entry.getTimestamp(), FHIR_TO_CADF_ACTION_MAP.getOrDefault(entry.getContext().getAction(), Action.unknown),
                        cadfEventOutCome)
                        .observer(observerRsrc)
                        .initiator(initiator).target(target)
                        .tag(entry.getCorrelationId()).attachment(new CadfAttachment("application/json", FHIRContext.FHIRWriter.generate(fhirContext))).build();
            } catch (IllegalStateException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        logger.exiting(CLASSNAME, METHODNAME);
        return this;
    }

    @Override
    public String serialize() {
        // TODO Auto-generated method stub
        return null;
    }
}