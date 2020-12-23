#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2020
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -o errexit
set -o nounset
set -o pipefail
set -x

ls -al /

/bin/kafka-console-consumer --timeout-ms 120000 --bootstrap-server=$(hostname):19092,kafka-2:29092 \
    --topic FHIR_AUDIT --max-messages 25 --property print.timestamp=true --offset earliest \
    --consumer.config /etc/kafka/secrets/client-ssl.properties --group fhir \
    --partition 1 > /output/fhir_audit-messages.log

# EOF