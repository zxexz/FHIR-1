/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.source;

import java.util.ArrayList;
import java.util.List;

import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.resource.Resource;

/**
 *
 */
public abstract class AbstractSource implements Source {
    private List<Resource> resources = new ArrayList<>();
    private long numOfParseFailures = 0;
    private String dataUrl = null;
    private long lines = 0;
    private ImportTransientUserData chunkData = null;

    public AbstractSource () {
        // No Operation
    }

    @Override
    public void setSource(String dataUrl, long lines, ImportTransientUserData chunkData) {
        this.dataUrl = dataUrl;
        this.lines = lines;
        this.chunkData = chunkData;
    }

    @Override
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public long numOfParseFailures() {
        return numOfParseFailures;
    }

    @Override
    public abstract void read();
}