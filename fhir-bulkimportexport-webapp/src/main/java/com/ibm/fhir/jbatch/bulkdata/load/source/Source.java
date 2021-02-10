/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.source;

import java.util.List;

import com.ibm.fhir.jbatch.bulkdata.load.data.ImportTransientUserData;
import com.ibm.fhir.model.resource.Resource;

/**
 *
 */
public interface Source {

    public void read(String dataUrl, long lines, ImportTransientUserData chunkData);

    public List<Resource> getResources();

    public long numOfParseFailures();
}
