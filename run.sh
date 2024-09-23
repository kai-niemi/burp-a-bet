#!/bin/bash

if [ ! -f "$jarfile" ]; then
    ./mvnw clean install
fi

java -jar ${jarfile} --spring.profiles.active=${profiles} ${params} $*

