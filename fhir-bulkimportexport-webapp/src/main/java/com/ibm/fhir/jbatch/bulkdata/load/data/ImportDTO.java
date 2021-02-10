/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.jbatch.bulkdata.load.data;

import java.util.List;

import com.ibm.fhir.model.resource.Resource;

/**
 *
 */
public class ImportDTO {
    private List<Resource> resources = null;

    /**
     * @param resources
     */
    public ImportDTO(List<Resource> resources) {
        super();
        this.resources = resources;
    }

    /**
     * @return the resources
     */
    public List<Resource> getResources() {
        return resources;
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}