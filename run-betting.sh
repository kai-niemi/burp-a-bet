#!/bin/bash

params="--spring.profiles.active=dev"

java -jar burpabet-betting-service/target/burpabet-betting-service.jar $params $*