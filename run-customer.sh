#!/bin/bash

# Remove __ prefix to enable
__params="\
--spring.datasource.url=jdbc:postgresql://192.168.1.99:26257/burp_customer?sslmode=disable \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.kafka.bootstrap-servers=192.168.1.99:9092 \
--spring.flyway.placeholders.cdc-sink-url=kafka://192.168.1.99:9092 \
--burp.wallet-api-url=http://localhost:8091/api"
# List of spring profiles (selected from menu)
profiles=
# Base dir for app module
basedir=burpabet-customer-service
# Executable jar
jarfile=${basedir}/target/customer-service.jar

######################################
# Do not edit below
######################################

source run-menu.sh
