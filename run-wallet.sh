#!/bin/bash

# Remove __ prefix to enable
__params="\
--spring.datasource.url=jdbc:postgresql://192.168.1.99:26257/burp_wallet?sslmode=disable \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.kafka.bootstrap-servers=192.168.1.99:9092 \
--spring.flyway.placeholders.cdc-sink-url=kafka://192.168.1.99:9092"
# List of spring profiles (selected from menu)
profiles=
# Base dir for app module
basedir=burpabet-wallet-service
# Executable jar
jarfile=${basedir}/target/wallet-service.jar

######################################
# Do not edit below
######################################

source run-menu.sh
