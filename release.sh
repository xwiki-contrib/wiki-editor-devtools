#!/bin/bash
PARAMETERS="-Pintegration-tests -DskipTests=true"

mvn release:prepare -U ${PARAMETERS} -DautoVersionSubmodules=true -Darguments="${PARAMETERS}" && mvn release:perform ${PARAMETERS} -Darguments="${PARAMETERS}"
