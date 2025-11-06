#!/bin/bash

params=
__params="\
--spring.datasource.url=jdbc:postgresql://192.168.1.99:26257/burp_customer?sslmode=disable \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.kafka.bootstrap-servers=192.168.1.99:9092 \
--spring.flyway.placeholders.cdc-sink-url=kafka://192.168.1.99:9092 \
--wallet-api-url=http://localhost:8091/api"
# List of spring profiles
profiles=local
# Base dir for app module
basedir=customer-service
# Executable jar
jarfile=${basedir}/target/customer-service.jar

######################################
# Do not edit below
######################################

source run.sh
