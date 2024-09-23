# Demo API

This guide describes initiating the three different journeys through
the REST APIs using only cURL.

## Register Customer

GET form:

    curl http://localhost:8090/api/registration/ 

Piped to file:

    curl http://localhost:8090/api/registration/ > registration.json

POST form back (idempotent):

    curl -v -d "@registration.json" -H "Content-Type:application/json" -X POST http://localhost:8090/api/registration/

## Place a bet

GET form:

    curl http://localhost:8092/api/placement/

Piped to file:

    curl http://localhost:8092/api/placement/ > placement.json

POST form back (idempotent if key is present):

    curl -v -d "@placement.json" -H "Content-Type:application/json" -X POST http://localhost:8092/api/placement/

## Settle bets

GET form:

    curl http://localhost:8092/api/settlement/ 
    curl http://localhost:8092/api/settlement/ > settlement.json

POST form back (non-idempotent):

    curl -v -d "@settlement.json" -H "Content-Type:application/json" -X POST http://localhost:8092/api/settlement/
