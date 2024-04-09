#!/bin/bash

java -jar betting-service.jar \
 --spring.profiles.active=none \
 --spring.datasource.url="jdbc:postgresql://odin-n37.aws-eu-west-1.cockroachlabs.cloud:26257/betting?sslmode=verify-full&sslrootcert=$HOME/Library/CockroachCloud/certs/10000000-0000-0000-0000-000000000000/odin-ca.crt" \
 --spring.datasource.username=burp \
 --spring.datasource.password=secret \
 --spring.kafka.bootstrap-servers="ec2-13-49-74-241.eu-west-1.compute.amazonaws.com:9092" \
 --spring.flyway.placeholders.cdc-sink-url="kafka://9.10.11.12:9092" \
$*
