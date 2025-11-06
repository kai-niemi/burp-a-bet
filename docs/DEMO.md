# Demo API

This guide describes how to run three different journeys through the REST APIs using cURL.

## Register Customer (using customer-service)

GET the request form:

    curl http://localhost:8090/api/registration/ > registration.json && cat registration.json | jq 

POST the form back and expect a 201:

    curl -v -d "@registration.json" -H "Content-Type:application/json" -X POST http://localhost:8090/api/registration/

## Place a bet (using betting-service)

GET the request form:

    curl http://localhost:8092/api/placement/ > placement.json && cat placement.json | jq

POST the form back:

    curl -v -d "@placement.json" -H "Content-Type:application/json" -X POST http://localhost:8092/api/placement/

## Settle bets (using betting-service)

GET the request form:

    curl http://localhost:8092/api/settlement/ > settlement.json && cat settlement.json | jq

POST the form back and expect a 201:

    curl -v -d "@settlement.json" -H "Content-Type:application/json" -X POST http://localhost:8092/api/settlement/
