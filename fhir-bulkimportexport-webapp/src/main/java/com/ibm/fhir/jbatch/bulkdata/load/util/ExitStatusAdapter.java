/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.jbatch.bulkdata.load.util;

import static com.ibm.fhir.jbatch.bulkdata.common.Constants.IMPORT_INPUT_RESOURCE_TYPE;
import static com.ibm.fhir.jbatch.bulkdata.common.Constants.IMPORT_INPUT_RESOURCE_URL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.ibm.fhir.jbatch.bulkdata.load.data.ImportCheckPointData;

/**
 * Adapts the partitionSummaries and DatasourceArray into an Exit Status
 */
public class ExitStatusAdapter {
    private static final Logger logger = Logger.getLogger(ExitStatusAdapter.class.getName());

    private JsonArray dataSourceArray = null;
    private List<ImportCheckPointData> partitionSummaries = null;

    public ExitStatusAdapter(JsonArray dataSourceArray, List<ImportCheckPointData> partitionSummaries) {
        this.dataSourceArray = dataSourceArray;
        this.partitionSummaries = partitionSummaries;
    }

    public String generateResultExitStatus() {
        Map<String, Integer> inputUrlSequenceMap = new HashMap<>();
        int sequnceNum = 0;
        for (JsonValue jsonValue : dataSourceArray) {
            JsonObject dataSourceInfo = jsonValue.asJsonObject();
            String DSTypeInfo = dataSourceInfo.getString(IMPORT_INPUT_RESOURCE_TYPE);
            String DSDataLocationInfo = dataSourceInfo.getString(IMPORT_INPUT_RESOURCE_URL);
            inputUrlSequenceMap.put(DSTypeInfo + ":" + DSDataLocationInfo, sequnceNum++);
        }

        String resultInExitStatus[] = new String[sequnceNum];
        for (ImportCheckPointData partitionSummary : partitionSummaries) {
            if (partitionSummary == null) {
                logger.warning("One or more partitionSummaries are null; results may be incomplete");
                continue;
            }
            int index = inputUrlSequenceMap.get(partitionSummary.getImportPartitionResourceType() + ":" + partitionSummary.getImportPartitionWorkitem());
            resultInExitStatus[index] = partitionSummary.getNumOfImportedResources() + ":" + partitionSummary.getNumOfImportFailures();
        }
        return Arrays.toString(resultInExitStatus);
    }
}