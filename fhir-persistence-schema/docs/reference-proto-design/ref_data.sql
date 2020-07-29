-------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2020
--
-- SPDX-License-Identifier: Apache-2.0
-------------------------------------------------------------------------------

-- Populate our prototype schema with some mock data to demonstrate how we
-- need to build the required FHIR search joins
SET CURRENT SCHEMA proto;

INSERT INTO resource_types (resource_type_id, resource_type_name) VALUES (1, 'Patient');
INSERT INTO resource_types (resource_type_id, resource_type_name) VALUES (2, 'Observation');

-- Define a minimal set of parameters for the example
INSERT INTO parameter_names VALUES (1, '_id');
INSERT INTO parameter_names VALUES (2, 'identifier');
INSERT INTO parameter_names VALUES (3, 'subject');
INSERT INTO parameter_names VALUES (4, 'accountRef');

-- The Logical Resources we want to link together with references
INSERT INTO logical_resources VALUES (1, 1, 'patient1',  'N');
INSERT INTO logical_resources VALUES (2, 1, 'patient2',  'N');
INSERT INTO logical_resources VALUES (3, 2, 'pat1-obs1', 'N');
INSERT INTO logical_resources VALUES (4, 2, 'pat1-obs2', 'N');
INSERT INTO logical_resources VALUES (5, 2, 'pat2-obs1', 'N');
INSERT INTO logical_resources VALUES (6, 2, 'pat2-obs2', 'N');
INSERT INTO logical_resources VALUES (7, 2, 'pat2-obs3', 'N');

-- Create 'patient' reference from Observation to Patient
INSERT INTO local_references VALUES (3, 3, 1);
INSERT INTO local_references VALUES (3, 4, 1);
INSERT INTO local_references VALUES (3, 5, 2);
INSERT INTO local_references VALUES (3, 6, 2);
INSERT INTO local_references VALUES (3, 7, 2);

-- Now for some external references. First we need a couple of systems
-- to encode the long URLs (which would otherwise be repeated)
INSERT INTO external_systems VALUES (1, 'http://fhir.system1.com/fhir-server/api/v4/Patient/');
INSERT INTO external_systems VALUES (2, 'http://fhir.system2.com/fhir-server/api/v4/Device/');

-- Relate Observation pat2-obs3 to an external Patient and Device
INSERT INTO external_references VALUES (3, 7, 1, '42-douglas');
INSERT INTO external_references VALUES (3, 7, 2, '23-apple');



