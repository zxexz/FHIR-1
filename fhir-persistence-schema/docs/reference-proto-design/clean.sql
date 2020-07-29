-------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2020
--
-- SPDX-License-Identifier: Apache-2.0
-------------------------------------------------------------------------------

SET CURRENT SCHEMA proto;
DELETE FROM external_references;
DELETE FROM external_systems;
DELETE FROM local_references;
DELETE FROM logical_resources;
DELETE FROM parameter_names;
DELETE FROM resource_types;

