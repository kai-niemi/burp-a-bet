#!/bin/bash

XXparams="\
--spring.profiles.active=local \
--spring.datasource.url=jdbc:postgresql://192.168.1.99:26257/burp_wallet?sslmode=disable \
--spring.datasource.username=root \
--spring.datasource.password= \
--spring.kafka.bootstrap-servers=192.168.1.99:9092 \
--spring.flyway.placeholders.cdc-sink-url=kafka://192.168.1.99:9092"

java -jar burpabet-wallet-service/target/wallet-service.jar $params $*
