```
-----------------------------------------------------------------------------
-- (C) Copyright IBM Corp. 2020
--
-- SPDX-License-Identifier: Apache-2.0
-----------------------------------------------------------------------------
```

# ISSUE-1366

Prototype design for modeling references more efficiently and include support for referential integrity if desired.

## Files

```
clean.sql    - Deletes all test rows populated by the ref_data.sql script
extref.sql   - Query demonstration a reference search for an external reference value
intref.sql   - Query demonstration a reference search for an internal reference value
intplan.txt  - Execution plan for the internal reference query
extplan.txt  - Execution plan for the external reference query
ref_data.sql - Mock data used to support demonstration joins and referential integrity rules
schema.sql   - DDL to create the prototype/demonstration tables and indexes
setup.sql    - Sets the current schema for use with dbexpln
```


## Apply and Run

```
db2 connect to fhirdb
db2 -tf setup.sql
db2 -tf schema.sql
db2 -tf ref_data.sql
db2 -tf intref.sql
db2 -tf extref.sql
```


## Explain Plan

```
db2expln -d fhirdb -setup setup.sql -g -z \; -f intref.sql -o intplan.txt 
db2expln -d fhirdb -setup setup.sql -g -z \; -f extref.sql -o extplan.txt 
```

