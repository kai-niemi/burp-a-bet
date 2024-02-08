#!/bin/bash

params="--spring.profiles.active=dev"

java -jar burpabet-betting-service/target/betting-service.jar $params $*

#java -jar burpabet-betting-service/target/betting-service.jar \
# --spring.profiles.active=local \
# --spring.datasource.url="jdbc:postgresql://192.168.1.99:26257/burp_betting?sslmode=disable" \
# --spring.datasource.username=root \
# --spring.datasource.password= \
# --spring.kafka.bootstrap-servers="192.168.1.99:9092" \
# --spring.flyway.placeholders.cdc-sink-url="kafka://192.168.1.99:9092" \
#$*
