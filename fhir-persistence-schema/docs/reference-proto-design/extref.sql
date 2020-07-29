-------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2020
--
-- SPDX-License-Identifier: Apache-2.0
-------------------------------------------------------------------------------

-- find all observations with a subject referring to an externa reference
SELECT lr.logical_id AS observation
  FROM external_systems xs
  JOIN external_references xr
    ON xs.external_system_name = 'http://fhir.system1.com/fhir-server/api/v4/Patient/'
   AND xr.external_system_id = xs.external_system_id
   AND xr.external_reference_value = '42-douglas'
   AND xr.parameter_name_id = 3
  JOIN logical_resources lr
    ON lr.logical_resource_id = xr.logical_resource_id
;
