#!/bin/bash

params="\
--spring.profiles.active=local \
--spring.datasource.url=jdbc:postgresql://192.168.1.99:26257/burp_customer?sslmode=disable \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.kafka.bootstrap-servers=192.168.1.99:9092 \
--spring.flyway.placeholders.cdc-sink-url=kafka://192.168.1.99:9092 \
--burp.wallet-api-url=http://localhost:8091/api"

java -jar burpabet-customer-service/target/customer-service.jar $params $*
