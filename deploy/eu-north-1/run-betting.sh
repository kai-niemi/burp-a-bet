#!/bin/bash

java -jar betting-service.jar \
 --spring.profiles.active=none \
 --spring.datasource.url="jdbc:postgresql://odin-n37.aws-eu-north-1.cockroachlabs.cloud:26257/burp_betting?sslmode=verify-full&sslrootcert=$HOME/Library/CockroachCloud/certs/10000000-0000-0000-0000-000000000000/odin-ca.crt" \
 --spring.datasource.username=burp \
 --spring.datasource.password=secret \
 --spring.kafka.bootstrap-servers="ec2-13-49-74-241.eu-north-1.compute.amazonaws.com:9092" \
 --spring.flyway.placeholders.cdc-sink-url="kafka://5.6.7.8:9092" \
$*
