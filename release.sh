#!/bin/bash

mvn release:prepare -Pintegration-tests,docker -DskipTests -Darguments="-DskipTests" && mvn release:perform -Pintegration-tests,docker -DskipTests -Darguments="-DskipTests"
