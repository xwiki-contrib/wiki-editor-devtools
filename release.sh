#!/bin/bash
PARAMETERS="-DskipTests=true -Dxwiki.enforcer.skip=true"

mvn release:prepare -U ${PARAMETERS} -DautoVersionSubmodules=true -Darguments="${PARAMETERS}" && mvn release:perform ${PARAMETERS} -Darguments="${PARAMETERS}"
