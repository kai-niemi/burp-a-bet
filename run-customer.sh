#!/bin/bash

params="--spring.profiles.active=dev"

java -jar burpabet-customer-service/target/burpabet-customer-service.jar $params $*