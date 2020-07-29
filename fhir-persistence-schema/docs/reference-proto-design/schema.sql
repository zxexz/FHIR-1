-------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2020
--
-- SPDX-License-Identifier: Apache-2.0
-------------------------------------------------------------------------------

-- Prototype model for a new Reference schema design

SET CURRENT SCHEMA proto;

CREATE TABLE resource_types (
  resource_type_id INT NOT NULL,
  resource_type_name VARCHAR(24) NOT NULL,
  CONSTRAINT pk_resource_types PRIMARY KEY (resource_type_id)
);

CREATE UNIQUE INDEX unq_resource_type_name ON resource_types(resource_type_name);

CREATE TABLE parameter_names (
  parameter_name_id INT NOT NULL,
  parameter_name VARCHAR(64) NOT NULL,
  CONSTRAINT pk_parameter_name PRIMARY KEY (parameter_name_id)
);

CREATE UNIQUE INDEX unq_parameter_name ON parameter_names(parameter_name);


-- The global logical resources table. Contains an entry for every
-- resource in the server
CREATE TABLE logical_resources (
  logical_resource_id         BIGINT     NOT NULL,
  resource_type_id               INT     NOT NULL,
  logical_id                 VARCHAR(64) NOT NULL,
  is_ghost                      CHAR(1)  DEFAULT 'N' NOT NULL,
  CONSTRAINT pk_logical_resource PRIMARY KEY (logical_resource_id),
  CONSTRAINT ck_host CHECK (is_ghost IN ('Y', 'N'))
);

-- find the logical resource entry. The order of this index is
-- important, because it allows us to find all logical resources
-- matching a given logical_id, even if we don't know the
-- resource type (needed for FHIR search)
CREATE UNIQUE INDEX unq_logical_id ON logical_resources (logical_id, resource_type_id);

-- Local references models the relationships between logical resources
-- within the server
CREATE TABLE local_references (
  parameter_name_id          INT NOT NULL,
  logical_resource_id     BIGINT NOT NULL, -- e.g. Observervation
  ref_logical_resource_id BIGINT NOT NULL  -- refering to Patient
);

-- Given the reference logical resource, find the mapped logical_resource_id
CREATE UNIQUE INDEX idx_local_references_prl ON local_references (ref_logical_resource_id, parameter_name_id, logical_resource_id);

-- So we can find the reference when we already know the logical resource
CREATE UNIQUE INDEX idx_local_references_lrid ON local_references (logical_resource_id, parameter_name_id);

ALTER TABLE local_references ADD CONSTRAINT fk_parameter_name_id FOREIGN KEY (parameter_name_id) REFERENCES parameter_names;
ALTER TABLE local_references ADD CONSTRAINT fk_ref_logical_resource_id FOREIGN KEY (ref_logical_resource_id) REFERENCES logical_resources;
ALTER TABLE local_references ADD CONSTRAINT fk_logical_resource_id FOREIGN KEY (logical_resource_id) REFERENCES logical_resources;

-- Support for external references. Separate the long system name (usually
-- a URL) just as we do for tokens. This normalization helps to keep the
-- size of the index down.
CREATE TABLE external_systems (
  external_system_id         BIGINT     NOT NULL,
  external_system_name      VARCHAR(64) NOT NULL,
  CONSTRAINT pk_external_system PRIMARY KEY (external_system_id)
);

CREATE UNIQUE INDEX unq_external_system_nm ON external_systems(external_system_name);

-- Similar to local_references, but in this case the reference is external.
-- We don't normalize the value, instead we just make it the first value in
-- the main index so that we don't have to repeat it everywhere
CREATE TABLE external_references (
  parameter_name_id            INT     NOT NULL,
  logical_resource_id       BIGINT     NOT NULL,
  external_system_id        BIGINT     NOT NULL,
  external_reference_value VARCHAR(64) NOT NULL
);

-- Support lookup to find the logical resources with the given external system/value pair
CREATE UNIQUE INDEX unq_external_references_prsr ON external_references (external_reference_value, parameter_name_id, external_system_id, logical_resource_id);

-- So we can find the reference when we already know the logical resource
-- We choose not to include the system/value here, because there will be relatively
-- few rows for a give logical_resource_id/parameter_name_id anyway...
CREATE INDEX idx_external_references_lrid ON external_references(logical_resource_id, parameter_name_id);

ALTER TABLE external_references ADD CONSTRAINT fk_parameter_name_id FOREIGN KEY (parameter_name_id) REFERENCES parameter_names;
ALTER TABLE external_references ADD CONSTRAINT fk_external_system_id FOREIGN KEY (external_system_id) REFERENCES external_systems;
ALTER TABLE external_references ADD CONSTRAINT fk_logical_resource_id FOREIGN KEY (logical_resource_id) REFERENCES logical_resources;
