/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.util;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.NDJSON_LINESEPERATOR;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportDTO;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.validation.FHIRValidator;
import com.ibm.fhir.validation.exception.FHIRValidationException;

/**
 * $import operation - Validation
 */
public class Validation {
    private static final Logger logger = Logger.getLogger(Validation.class.getName());

    private boolean collectImportOperationOutcomes = true;
    private long validationStartTimeInMilliSeconds = 0;
    private long failedNum = 0;
    private ImportTransientUserData chunkData = null;
    private Set<String> failedValidationIds = new HashSet<>();

    public Validation(ImportTransientUserData chunkData, boolean collectImportOperationOutcomes) {
        this.chunkData = chunkData;
        this.collectImportOperationOutcomes = collectImportOperationOutcomes;
    }

    public void validate(ImportDTO dto) {
        for (Resource fhirResource : dto.getResources()) {
            try {
                validateInput(fhirResource);
            } catch (FHIRValidationException|FHIROperationException e) {
                logger.warning("Failed to validate '" + fhirResource.getId() + "' due to error: " + e.getMessage());
                failedNum++;
                failedValidationIds.add(fhirResource.getId());

                if (collectImportOperationOutcomes) {
                    OperationOutcome operationOutCome = FHIRUtil.buildOperationOutcome(e, false);
                    FHIRGenerator.generator(Format.JSON).generate(operationOutCome, chunkData.getBufferStreamForImportError());
                    chunkData.getBufferStreamForImportError().write(NDJSON_LINESEPERATOR);
                }
            }
        }
    }

    /**
     * Validate the input resource and throw if there are validation errors
     *
     * @param resource
     * @throws FHIRValidationException
     * @throws FHIROperationException
     */
    public List<OperationOutcome.Issue> validateInput(Resource resource)
            throws FHIRValidationException, FHIROperationException {
        List<OperationOutcome.Issue> issues = FHIRValidator.validator().validate(resource);
        if (!issues.isEmpty()) {
            boolean includesFailure = false;
            for (OperationOutcome.Issue issue : issues) {
                if (FHIRUtil.isFailure(issue.getSeverity())) {
                    includesFailure = true;
                }
            }

            if (includesFailure) {
                throw new FHIROperationException("Input resource failed validation.").withIssue(issues);
            } else if (logger.isLoggable(Level.FINE)) {
                    String info = issues.stream()
                                .flatMap(issue -> Stream.of(issue.getDetails()))
                                .flatMap(details -> Stream.of(details.getText()))
                                .flatMap(text -> Stream.of(text.getValue()))
                                .collect(Collectors.joining(", "));
                    logger.fine("Validation warnings for input resource: [" + info + "]");
            }
        }
        return issues;
    }

    public long totalTime() {
        return (System.currentTimeMillis() - validationStartTimeInMilliSeconds);
    }

    public long failedNum() {
        return failedNum;
    }

    public boolean didResourcePassValidation(String id) {
        return failedValidationIds.contains(id);
    }

    public void updateTotalValidation() {
        chunkData.addToTotalValidationMilliSeconds(totalTime());
    }
}