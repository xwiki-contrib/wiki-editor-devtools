#!/bin/bash
PARAMETERS="-DskipTests=true"

mvn release:prepare -U ${PARAMETERS} -DautoVersionSubmodules=true -Darguments="${PARAMETERS}" && mvn release:perform ${PARAMETERS} -Darguments="${PARAMETERS}"
