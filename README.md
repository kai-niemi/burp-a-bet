# Burp-a-bet 

[![Java CI with Maven](https://github.com/kai-niemi/burp-a-bet/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/kai-niemi/burp-a-bet/actions/workflows/maven.yml)

<!-- TOC -->
* [Burp-a-bet](#burp-a-bet-)
* [Introduction](#introduction)
* [Design Notes](#design-notes)
* [Building](#building)
  * [Prerequisites](#prerequisites)
  * [Setup](#setup)
    * [Clone the project](#clone-the-project)
    * [Build the executable jars](#build-the-executable-jars)
* [Demo Tutorial](#demo-tutorial)
  * [Demo Setup](#demo-setup)
    * [Setup CockroachDB](#setup-cockroachdb)
      * [Create the databases](#create-the-databases)
    * [Setup and Start Kafka](#setup-and-start-kafka)
    * [Start Services](#start-services)
  * [Custom Parameters (optional)](#custom-parameters-optional)
  * [Demo Commands](#demo-commands)
    * [Customer Service](#customer-service)
    * [Wallet Service](#wallet-service)
    * [Betting Service](#betting-service)
  * [Appendix](#appendix)
    * [API Testing](#api-testing)
    * [Rule Invariants](#rule-invariants)
    * [Additional Resources](#additional-resources)
* [Terms of Use](#terms-of-use)
<!-- TOC -->

<img align="left" src="logo.png" width="128" height="128" />

Welcome to Burp-a-Bet horse betting - a voice activated online betting system demo based 
on CockroachDB, Kafka and Spring Boot. The voice activation part is work in progress. 
For now all bets are placed by keystrokes in the embedded interactive shell.

# Introduction
 
The system is designed to _demonstrate_ different architectural patterns and mechanisms in the context 
of an Online Sports Betting use case. In particular distributed business transactions using Sagas over 
XA/2PC.

The purpose is not to accurately model the full domain complexity of sports betting, but to give an idea 
of how the mechanics of such systems could be crafted using [CockroachDB](https://www.cockroachlabs.com/) as the SoR database of 
choice.

The system provides three independent microservices that together supports the following customer journeys:
 
- **Customer Registration** - where a player registers with a betting operator
- **Bet Placement** - where a player wagers a bet on a specific game (track and horse)
- **Bet Settlement** - where open bets are settled with a win or loss

# Design Notes

To promote service autonomy, independence and availability, all journeys aka business transactions 
are modeled using [Sagas](https://microservices.io/patterns/data/saga.html) using the orchestration method.  

Think of this pattern as a decomposed two-phase commit protocol providing eventual consistency between
independent service through asynchronous message exchange. This fits well into the microservice architecture
style where strong ACID transactions are typically scoped to the bounded contexts. Journeys spanning
between services are typically coordinated asynchronously without ACID / blocking protocols to promote 
decoupling, scalability and availability.

The downside with Saga's is that it adds complexity to the architecture and blurs out the state
transitions in customer journeys, making it less visible and harder to trace flows. There are plenty
of different application frameworks for using Saga's at a larger scale. This demo however focuses
mainly on the primitives for using Saga's including message passing and local ACID transactions.

All services maintain their local state in an isolated database using [ACID](https://en.wikipedia.org/wiki/ACID) guarantees and 
local transactions. The message exchange between the services are customer journey state transitions. 
Messages are passed through the transactional outbox pattern where [CDC queries](https://www.cockroachlabs.com/docs/stable/cdc-queries) are used in 
combination with [Kafka stream joins](https://kafka.apache.org/documentation/streams/) to stitch together requests with responses.  

This makes the journeys fully asynchronous and transactionally safe in terms of safeguarded rule 
invariants. The outbox pattern, for example, guarantees at-least-once semantics in the message passing
and that no events are emitted if transactions fail. There can't be any out-of-sync message passing
where an event is emitted and then the transaciton rolls back. See the rule invariants section below 
for a more precise meaning of _safety_ in this context.

In summary, the system demonstrates the following mechanisms in CockroachDB:

* [CDC Queries](https://www.cockroachlabs.com/docs/stable/cdc-queries) - each service has an outbox table and CDC projection query to send events to Kafka.
* [Row-level TTL eviction](https://www.cockroachlabs.com/docs/v23.2/row-level-ttl) - deletes expired outbox event records.
* [Follower reads](https://www.cockroachlabs.com/docs/v23.2/follower-reads) - used by REST endpoints to inspect betting and race data without interfering with ongoing 
journeys (causing retries).
* [Multi-region (optional)](https://www.cockroachlabs.com/docs/v23.2/table-localities#regional-by-row-tables) - using regional-by-row to pin accounts and bets to specific jurisdictions. 
* Computed virtual columns and enum types

All three services provide an interactive shell and a [REST API](https://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven) using the [HAL+forms](https://rwcbook.github.io/hal-forms/) hypermedia type.

The interactive shells are used to initiate the different journeys described above and other management tasks. 
The APIs are used for observability and also for initiating journeys using HTTP requests through cURL / 
Postman or similar tools.

# Building

The project builds executable JAR files for each deployable component or microservice. 
These JAR files runs on any platform for which there is a Java 21+ runtime.
                 
## Prerequisites

- JDK 21 (LTS)
    - https://openjdk.org/projects/jdk/21/
    - https://www.oracle.com/java/technologies/downloads/#java21
- Maven 3+ (optional, embedded wrapper available)
    - https://maven.apache.org/
- CockroachDB 23.1+ with an Enterprise License
  - https://www.cockroachlabs.com/docs/releases/
- Kafka 3.6+
  - https://kafka.apache.org/downloads

## Setup

Install the JDK (Ubuntu example):

    sudo apt-get install openjdk-21-jdk

Install the JDK (MacOS example using sdkman):

    curl -s "https://get.sdkman.io" | bash
    sdk list java
    sdk install java 21.0..  

Confirm the installation by running:

    java --version

### Clone the project
                             
    git clone git@github.com:kai-niemi/burp-a-bet.git burp-a-bet

### Build the executable jars

    cd burp-a-bet
    chmod +x mvnw
    ./mvnw clean install

The executable jars are now found under each respective module's `target` directory.

# Demo Tutorial

This section describes how to run a local demo on MacOS.

> For multi-region deployments using CockroachCloud on either AWS, GCP or Azure, there are
prepared template script and SQL files in the [scripts](scripts/) directory. It describes a
manual process for a bit more advanced demos involving multi-region patterns.

## Demo Setup

### Setup CockroachDB

For deploying a local CockroachDB cluster, see 
https://www.cockroachlabs.com/docs/v24.2/start-a-local-cluster.

#### Create the databases

Create the following databases, one for each service:

    cockroach sql --insecure --host=localhost -e "CREATE database burp_wallet"
    cockroach sql --insecure --host=localhost -e "CREATE database burp_customer"
    cockroach sql --insecure --host=localhost -e "CREATE database burp_betting"

Enable [range feeds](https://www.cockroachlabs.com/docs/stable/create-and-configure-changefeeds#enable-rangefeeds):

    cockroach sql --insecure --host=localhost -e "SET CLUSTER SETTING kv.rangefeed.enabled = true"

### Setup and Start Kafka

Kafka Streams is required to drive the distributed business transactions on top of CockroachDB 
CDC outbox events. You can either use a manged Kafka cluster or a local self-hosted setup. In 
the latter case, just follow the [quickstart](https://kafka.apache.org/quickstart) guidelines to setup a vanilla Kafka instance.

Depending on your network setup, you may need to edit the following in `config/server.properties`:

    listeners=PLAINTEXT://..
    advertised.listener=PLAINTEXT://

Then start Kafka in daemon mode:

    bin/kafka-server-start.sh -daemon config/server.properties

### Start Services

Burp-a-bet provides both built-in command line shells and REST (hypermedia driven) API endpoints
in each service. The shell is used for demo purposes to initiate the different journeys. The REST 
APIs are for observability and for command completion in the shells.

Start the services in three separate shell sessions (order does not matter):

Terminal 1:
    
    ./run-customer.sh

Terminal 2:

    ./run-wallet.sh
    
Terminal 3:

    ./run-betting.sh
    
Now you should have all three services up and running locally and listening on the following ports:

| Service  | Shell | API / Front-end       | Capability                                                                                  |
|----------|-------|-----------------------|---------------------------------------------------------------------------------------------|
| customer | yes   | http://localhost:8090 | Orchestrates the registration journey                                                       |
| wallet   | yes   | http://localhost:8091 | Manages monetary accounts for customers and operators using a double-entry financial ledger |
| betting  | yes   | http://localhost:8092 | Orchestrates the bet placement and settlement journeys                                      |

You can verify with curl:

    curl 'http://localhost:8090' -i -X GET
    curl 'http://localhost:8091' -i -X GET
    curl 'http://localhost:8092' -i -X GET

_Hint: if you are using Chrome, then [Json Viewer](https://chromewebstore.google.com/detail/json-viewer/gbmdgpbipfallnflgajpaliibnhdgobh) is a must-have._

## Custom Parameters (optional)

See [common-application-properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html) on how to tailor the application context. 
All parameters can be overridden through the CLI. 

For example:

    java -jar wallet-service.jar \
    --spring.profiles.active=local \
    --spring.datasource.url="jdbc:postgresql://my_fancy_cluster.aws-eu-north-1.cockroachlabs.cloud:26257/burp_wallet?sslmode=verify-full&sslrootcert=$HOME/Library/CockroachCloud/certs/<uuid>/cluster-name-ca.crt" \
    --spring.datasource.username=burp \
    --spring.datasource.password=*** \
    --spring.kafka.bootstrap-servers=<kafka-local-ip>:9092 \
    --spring.flyway.placeholders.cdc-sink-url=kafka://<kafka-public-ip>:9092 \
    $*

To run any of the service in the background without an interactive shell, you can 
put the above in a script and use the `--noshell` arg:

    nohup ./run-wallet.sh --noshell > wallet.txt &

## Demo Commands

In the different shells, type `help` for command guidance (or TAB for code completion).

### Customer Service
 
The customer service orchestrates the **registration** journey. Upon a customer registration, 
an outbox event is sent to the wallet and betting service through CDC. These services then 
do their stuff and either approves or rejects the registration, which is funneled back
to the customer service through a stream join. 

At registration:

- The betting service validates the jurisdiction.
- The wallet service creates a customer account and operator account if needed, and grants a registration bonus.

If both services accept the registration, it is approved. If any service rejects it, the customer 
service sends a compensating rollback request.

At rollback:

- The betting service does nothing.
- The wallet service reverts the welcome bonus but keeps the accounts.

To register 10 customers (default is 1), type:

    register --count 10

For more help, type:

    help register

### Wallet Service

The wallet service does not orchestrate any journeys, only participates in them. It provides a 
financial ledger using double-entry principles and an account plan for customers and operators. 

- Operators have _liability_ accounts that can have a negative balance. 
- Customers have _expense_ accounts that can only have a positive balance. 
- Funds are transferred only between operator and customer accounts, thus the total balance 
of all accounts must always equal zero.

For more help, type:

    help

### Betting Service

The betting service orchestrates the **bet placement** and **bet settlement** journeys. 

**Bet placement**

Upon placing a bet, an outbox event is sent to the wallet and customer service.
These services do their stuff and either approves or rejects the placement.

At placement:

- The customer service validates the spending budget is not exceeded (spending limit).
- The wallet service reserves the bet wager from the customers account (if enough funds) 
to the operator account.

If both services accept, the placement is approved. If any service rejects it, the betting
service sends a compensating rollback request.

At rollback:

- The customer service returns the acquired spending credits.
- The wallet service reverts the bet wager and moves funds back to the customer account.

To place 10 bets for a random customer on a random race, type:

    place-bets --count 10

To settle all bets, type:

    settle-bets

On settlement:

- The customer service does nothing
- The wallet service pays out a reward on wins

For more help, type:

    help <commnd>

## Appendix

### API Testing

The services also provide REST APIs for initiating the bet placement, bet settlement 
and registration journeys. See [API Demo](docs/DEMO.md) for a tutorial.

### Rule Invariants

In terms of measuring correct execution and outcomes during disruptions and/or contention, 
these are the main business rule invariants to observe:

Wallet service:

- Total sum of all accounts must always be zero
- Customer accounts must always have a balance of zero or higher
- Operator accounts can have both positive and negative balance

Betting service:

- Settled bets are marked as `settled` after payouts are transferred to customer account.
- Bet placement can only be done against registered customer accounts.
- Bet wagering can only be done against unsettled races.

Customer service:

- Customers spending budget (like a rate limit) must always be positive.

### Additional Resources

- [Design Notes](docs/README.md) - Details including architectural patterns and mechanisms.
- [Service Description](docs/diagrams.png) - [C4 model](https://c4model.com/) diagrams drawn using [okso](https://okso.app/) _(open the [diagrams.okso](docs/diagrams.okso) file)_.
- [Registration Journey Diagram](docs/registration-sequence.png) - Using a [websequence](https://www.websequencediagrams.com/) diagram.

# Terms of Use

See [MIT](LICENSE.txt) for terms and conditions.
