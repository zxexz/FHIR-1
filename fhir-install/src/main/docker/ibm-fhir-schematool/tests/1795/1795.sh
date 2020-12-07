#!/usr/bin/env bash

# ----------------------------------------------------------------------------
# (C) Copyright IBM Corp. 2020
#
# SPDX-License-Identifier: Apache-2.0
# ----------------------------------------------------------------------------

# Issue - https://github.com/IBM/FHIR/issues/1795 

# Pre Condition:
# 1 - ibmcom/ibm-fhir-schematool must be built based on the latest.
#       docker build -t ibmcom/ibm-fhir-schematool:latest .

# Post Condition: 
# 1 - Postgres should be started
# 2 - Tool should complete without any issues/exceptions
# 3 - The following schemas should exist
#        fhir_admin
#        fhirdata3
#        fhiroauth3
#        fhirbatch3

docker-compose up -d db

docker-compose run tool --tool.behavior=onboard --db.type=postgresql  \
    --db.host=db --db.port=5432 --db.database=fhirdb --schema.name.fhir=fhirdata3 \
    --schema.name.batch=fhirbatch3 --schema.name.oauth=fhiroauth3 \
    --user=postgres --password=change-password

echo SELECT schema_name FROM information_schema.schemata | docker-compose exec -T -e PGPASSWORD=change-password db psql -h db -U postgres fhirdb

docker-compose down -t 1
# EOF