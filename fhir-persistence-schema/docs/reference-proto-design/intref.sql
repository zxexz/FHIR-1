-------------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2020
--
-- SPDX-License-Identifier: Apache-2.0
-------------------------------------------------------------------------------

-- find all observations with a subject referring to Patient/patient1
SELECT lr.logical_id AS observation
  FROM logical_resources reference
  JOIN local_references map
    ON reference.logical_id = 'patient1'
   AND reference.is_ghost = 'N'
   AND reference.resource_type_id = 1
   AND map.ref_logical_resource_id = reference.logical_resource_id
  JOIN logical_resources lr
    ON lr.logical_resource_id = map.logical_resource_id
;
