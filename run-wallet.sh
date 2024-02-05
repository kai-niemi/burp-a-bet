#!/bin/bash

params="--spring.profiles.active=dev"

java -jar burpabet-wallet-service/target/burpabet-wallet-service.jar $params $*