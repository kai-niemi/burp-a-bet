#!/bin/bash

params="--spring.profiles.active=dev"

java -jar burpabet-customer-service/target/customer-service.jar $params $*