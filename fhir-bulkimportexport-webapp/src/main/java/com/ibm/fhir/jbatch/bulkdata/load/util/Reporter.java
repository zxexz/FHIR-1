/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.batch.runtime.JobExecution;
import javax.batch.runtime.context.JobContext;

import com.ibm.fhir.jbatch.bulkdata.load.data.ImportCheckPointData;

/**
 * Generates the Report for the Import Job
 */
public class Reporter {
    private static final Logger logger = Logger.getLogger(Reporter.class.getName());

    private long currentExecutionStartTimeInMS;
    private long currentExecutionEndTimeInMS;
    private long totalJobExecutionMilliSeconds = 0;

    public void start() {
        currentExecutionStartTimeInMS = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public void end() {
        currentExecutionEndTimeInMS = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public void calculateTotal(JobContext jobContext, List<JobExecution> jobExecutions) {
        // The job can be started, stopped and then started again, so we need to add them all to get the whole job execution duration.
        for ( JobExecution jobExecution: jobExecutions) {
            // For current execution, jobExecution.getEndTime() is either null or with wrong value because the current execution is not
            // finished yet, so always use system time for both job execution start time and end time.
            if (jobExecution.getExecutionId()  == jobContext.getExecutionId()) {
                totalJobExecutionMilliSeconds += (currentExecutionEndTimeInMS - currentExecutionStartTimeInMS);
            } else {
                totalJobExecutionMilliSeconds += (jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime());
            }
        }
    }

    public void report(List<ImportCheckPointData> partitionSummaries) {

        // Used for generating performance measurement per each resource type.
        Map<String, ImportCheckPointData> importedResourceTypeSummaries = new HashMap<>();

        for (ImportCheckPointData partitionSummary : partitionSummaries) {
            ImportCheckPointData partitionSummaryInMap = importedResourceTypeSummaries.get(partitionSummary.getImportPartitionResourceType());
            if (partitionSummaryInMap == null) {
                importedResourceTypeSummaries.put(partitionSummary.getImportPartitionResourceType(), partitionSummary);
            } else {
                partitionSummaryInMap.setNumOfImportFailures(partitionSummaryInMap.getNumOfImportFailures() + partitionSummary.getNumOfImportFailures());
                partitionSummaryInMap.setNumOfImportedResources(partitionSummaryInMap.getNumOfImportedResources() + partitionSummary.getNumOfImportedResources());
                partitionSummaryInMap.setNumOfProcessedResources(partitionSummaryInMap.getNumOfProcessedResources() + partitionSummary.getNumOfProcessedResources());
                partitionSummaryInMap.setTotalReadMilliSeconds(partitionSummaryInMap.getTotalReadMilliSeconds() + partitionSummary.getTotalReadMilliSeconds());
                partitionSummaryInMap.setTotalValidationMilliSeconds(partitionSummaryInMap.getTotalValidationMilliSeconds() + partitionSummary.getTotalValidationMilliSeconds());
                partitionSummaryInMap.setTotalWriteMilliSeconds(partitionSummaryInMap.getTotalWriteMilliSeconds() + partitionSummary.getTotalWriteMilliSeconds());
                partitionSummaryInMap.setImportFileSize(partitionSummaryInMap.getImportFileSize() + partitionSummary.getImportFileSize());
            }
        }

        double jobProcessingSeconds = totalJobExecutionMilliSeconds / 1000.0;
        jobProcessingSeconds = jobProcessingSeconds < 1 ? 1.0 : jobProcessingSeconds;

        // log the simple metrics.
        logger.info(" ---- FHIR resources imported in " + jobProcessingSeconds + "seconds ----");
        logger.info("ResourceType \t| Imported \t| Failed \t| TotalReadMilliSeconds \t| TotalWriteMilliSeconds \t| TotalValidationMilliSeconds"
                    + " \t| TotalSize \t| AverageSize");
        int totalImportedFhirResources = 0;
        for (ImportCheckPointData importedResourceTypeSummary : importedResourceTypeSummaries.values()) {
            logger.info(importedResourceTypeSummary.getImportPartitionResourceType() + "\t|"
                        + importedResourceTypeSummary.getNumOfImportedResources() + "\t|"
                        + importedResourceTypeSummary.getNumOfImportFailures() + "\t|"
                        + importedResourceTypeSummary.getTotalReadMilliSeconds() + "\t|"
                        + importedResourceTypeSummary.getTotalWriteMilliSeconds() + "\t|"
                        + importedResourceTypeSummary.getTotalValidationMilliSeconds() + "\t|"
                        + importedResourceTypeSummary.getImportFileSize() + "\t|"
                        + importedResourceTypeSummary.getImportFileSize()/(importedResourceTypeSummary.getNumOfImportedResources() + importedResourceTypeSummary.getNumOfImportFailures()));
            totalImportedFhirResources += importedResourceTypeSummary.getNumOfImportedResources();
        }
        logger.info(" ---- Total: " + totalImportedFhirResources
                + " ImportRate: " + new DecimalFormat("#0.00").format(totalImportedFhirResources/jobProcessingSeconds) + " ----");
    }
}