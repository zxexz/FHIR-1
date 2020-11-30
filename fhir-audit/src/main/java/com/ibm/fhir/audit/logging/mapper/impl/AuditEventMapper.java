/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.audit.logging.mapper.impl;

import static com.ibm.fhir.model.type.Code.code;
import static com.ibm.fhir.model.type.String.string;
import static com.ibm.fhir.model.type.Uri.uri;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.fhir.audit.logging.beans.AuditLogEntry;
import com.ibm.fhir.audit.logging.exception.FHIRAuditException;
import com.ibm.fhir.audit.logging.mapper.Mapper;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.AuditEvent;
import com.ibm.fhir.model.type.Coding;

/**
 * The AuditEventMapper maps the AuditLogEntry to the FHIR standard format.
 *
 * @link https://www.hl7.org/fhir/auditevent.html
 */
public class AuditEventMapper implements Mapper {

    private static final Logger logger = java.util.logging.Logger.getLogger(CADFMapper.class.getName());
    private static final String CLASSNAME = CADFMapper.class.getName();

    //@formatter:off
    private static final Coding TYPE = Coding.builder()
            .code(code("rest"))
            .display(string("Restful Operation"))
            .system(uri("http://terminology.hl7.org/CodeSystem/audit-event-type"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_READ = Coding.builder()
            .code(code("read"))
            .display(string("Read the current state of the resource."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_VREAD = Coding.builder()
            .code(code("vread"))
            .display(string("Read the state of a specific version of the resource."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_UPDATE = Coding.builder()
            .code(code("update"))
            .display(string("Update an existing resource by its id (or create it if it is new)."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_PATCH = Coding.builder()
            .code(code("patch"))
            .display(string("Update an existing resource by posting a set of changes to it."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_DELETE = Coding.builder()
            .code(code("delete"))
            .display(string("Delete a resource."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_HISTORY = Coding.builder()
            .code(code("history"))
            .display(string("Retrieve the change history for a particular resource, type of resource, or the entire system."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_HISTORY_INSTANCE = Coding.builder()
            .code(code("history-instance"))
            .display(string("Retrieve the change history for a particular resource."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_HISTORY_TYPE = Coding.builder()
            .code(code("history-type"))
            .display(string("Retrieve the change history for all resources of a particular type."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_HISTORY_SYSTEM = Coding.builder()
            .code(code("history-system"))
            .display(string("Retrieve the change history for all resources on a system."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_CREATE = Coding.builder()
            .code(code("create"))
            .display(string("Create a new resource with a server assigned id."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_SEARCH = Coding.builder()
            .code(code("search"))
            .display(string("Search a resource type or all resources based on some filter criteria."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_SEARCH_TYPE = Coding.builder()
            .code(code("search-type"))
            .display(string("Search all resources of the specified type based on some filter criteria."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_SEARCH_SYSTEM = Coding.builder()
            .code(code("search-system"))
            .display(string("Search all resources based on some filter criteria."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_CAPABILITIES = Coding.builder()
            .code(code("capabilities"))
            .display(string("Get a Capability Statement for the system."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_TRANSACTION = Coding.builder()
            .code(code("transaction"))
            .display(string("Update, create or delete a set of resources as a single transaction."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_BATCH = Coding.builder()
            .code(code("batch"))
            .display(string("Perform a set of a separate interactions in a single http operation"))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    //@formatter:off
    private static final Coding SUBTYPE_OPERATION = Coding.builder()
            .code(code("operation"))
            .display(string("Perform an operation as defined by an OperationDefinition."))
            .system(uri("http://hl7.org/fhir/restful-interaction"))
            .build();
    //@formatter:on

    private static final Map<AuditLogEventType,Coding> MAP_TO_SUBTYPE = buildMapToSubtype();

    private AuditEvent.Builder builder = AuditEvent.builder();

    @Override
    public Mapper map(AuditLogEntry entry) {
        final String METHODNAME = "map";
        logger.entering(CLASSNAME, METHODNAME);

        // Everything on this server is an initiated RESTFUL Operation
        // @link https://www.hl7.org/fhir/valueset-audit-event-type.html
        //@formatter:off
        builder.type(TYPE)
            //
            .subtype(subtype(entry));
        //@formatter:on

        action(entry);
        period(entry);
        recorded(entry);
        outcome(entry);
        outcomeDesc(entry);
        purposeOfEvent(entry);
        agent(entry);
        source(entry);
        entity(entry);
        logger.exiting(CLASSNAME, METHODNAME);
        return this;
    }

    @Override
    public String serialize() throws FHIRAuditException {
        AuditEvent auditEvent = builder.build();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FHIRGenerator.generator(Format.JSON).generate(auditEvent, out);
            return out.toString();
        } catch (Exception e) {
            throw new FHIRAuditException("Failed to serialize the Audit Event", e);
        }
    }

    private static Map<AuditLogEventType,Coding> buildMapToSubtype() {

    }

    /*
     *
     * @link https://www.hl7.org/fhir/valueset-audit-event-sub-type.html#expansion
     */
    private List<Coding> subtype(AuditLogEntry entry) {
        List<Coding> subtypes = new ArrayList<>();
        switch(entry.getEventType()) {
            "fhir-create":

                break;

        }
        return subtypes;
    }

    private void entity(AuditLogEntry entry) {
    }

    private void source(AuditLogEntry entry) {
    }

    private void agent(AuditLogEntry entry) {

    }

    private void purposeOfEvent(AuditLogEntry entry) {

    }

    private void outcomeDesc(AuditLogEntry entry) {

    }

    private void outcome(AuditLogEntry entry) {

    }

    private void recorded(AuditLogEntry entry) {

    }

    private void period(AuditLogEntry entry) {

    }

    private void action(AuditLogEntry entry) {

    }

}