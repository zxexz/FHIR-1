/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.load;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.IMPORT_FHIR_DATASOURCES;

import java.util.List;

import javax.batch.api.BatchProperty;
import javax.batch.api.listener.JobListener;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.JsonArray;

import com.ibm.fhir.jbatch.bulkdata.common.BulkDataUtils;
import com.ibm.fhir.jbatch.bulkdata.load.data.ImportCheckPointData;
import com.ibm.fhir.jbatch.bulkdata.load.util.ExitStatusAdapter;
import com.ibm.fhir.jbatch.bulkdata.load.util.Reporter;

@Dependent
public class Listener implements JobListener {
    private Reporter reporter = new Reporter();

    @Inject
    JobContext jobContext;

    @Inject
    @BatchProperty(name = IMPORT_FHIR_DATASOURCES)
    String dataSourcesInfo;

    public Listener() {
        // No Operation
    }

    @Override
    public void beforeJob() {
        reporter.start();
    }

    @Override
    public void afterJob() {
        reporter.end();

        JobOperator jobOperator = BatchRuntime.getJobOperator();
        reporter.calculateTotal(jobContext,
                                jobOperator.getJobExecutions(
                                    jobOperator.getJobInstance(jobContext.getExecutionId())));

        // Used for generating response for all the import data resources.
        @SuppressWarnings("unchecked")
        List<ImportCheckPointData> partitionSummaries = (List<ImportCheckPointData>) jobContext.getTransientUserData();

        // If the job is stopped before any partition is finished, then nothing to show.
        if (partitionSummaries == null) {
            return;
        }

        // Generate import summary and pass it into ExitStatus of the job execution.
        // e.g, [3:0, 4:1] means 3 resources imported and 0 failures for the 1st file, and 4 imported and 1 failure for
        // the 2nd file.
        JsonArray dataSourceArray = BulkDataUtils.getDataSourcesFromJobInput(dataSourcesInfo);

        ExitStatusAdapter adapter = new ExitStatusAdapter(dataSourceArray, partitionSummaries);
        jobContext.setExitStatus(adapter.generateResultExitStatus());

        reporter.report(partitionSummaries);
    }
}