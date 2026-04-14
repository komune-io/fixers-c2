## Project Overview

fixers-c2 — Kotlin SDK for [Blockchain-SSM](https://github.com/civis-blockchain/blockchain-ssm) (Signing State Machines) chaincode on Hyperledger Fabric. Libraries for blockchain ops, CouchDB query optimization, Spring Boot integration.

## Build Commands

```bash
# Lint (detekt static analysis)
make lint

# Build (compile + publish to Maven local, no tests)
make build

# Run all tests (requires dev environment running)
make test

# Start dev environment before tests
make test-pre

# Single module test
./gradlew :c2-ssm:ssm-sdk:ssm-sdk-core:test

# Single test class
./gradlew :c2-ssm:ssm-sdk:ssm-sdk-core:test --tests "io.komune.c2.ssm.sdk.core.SsmClientTest"
```

## Development Environment

Docker Compose sandbox — local Hyperledger Fabric network:

```bash
make dev up          # Start all sandbox services
make dev down        # Stop all services
make dev logs        # Follow logs
make dev c2-sandbox-ssm logs  # Logs for specific service
```

Services: `c2-sandbox`, `c2-sandbox-gateway`, `c2-sandbox-ssm`, `c2-sandbox-ex02`

## Architecture

### Module Structure

- **c2-ssm/** - Core SDK libraries
  - `ssm-sdk/` - Core client, signing, JSON serialization
  - `ssm-chaincode/` - Blockchain chaincode ops (query/invoke)
  - `ssm-couchdb/` - CouchDB query optimization for expensive blockchain queries
  - `ssm-data/` - Combines chaincode + CouchDB for complex queries
  - `ssm-tx/` - Transaction mgmt (create SSM, start/perform sessions)
  - `ssm-spring/` - Spring Boot starters per module
  - `ssm-bdd/` - Cucumber BDD test infra

- **c2-chaincode/** - Blockchain infra
  - `chaincode-api/gateway` - REST API gateway (builds Docker image)
  - `chaincode-ssm/` - SSM chaincode impl

- **c2-sandbox/** - Pre-configured Fabric network for testing

### F2 Pattern

Functions follow F2 (Function Factory) pattern for Spring DI:
- DSL modules define data models + function interfaces
- F2 modules implement functions as Spring beans
- Client modules wrap F2 functions for easy consumption

### Spring Boot Starters

Import starters for auto-configured beans:
```kotlin
implementation("io.komune.c2:ssm-chaincode-spring-boot-starter:${version}")
implementation("io.komune.c2:ssm-couchdb-spring-boot-starter:${version}")
implementation("io.komune.c2:ssm-data-spring-boot-starter:${version}")
```

## SSM Chaincode Operations

Key blockchain ops:
- **register** - Register user w/ admin signature
- **create** - Create new SSM w/ admin signature
- **start** - Start SSM session w/ admin signature
- **perform** - User performs action on session w/ signature
- **Queries** - session, ssm, user, admin, list, log, transaction, block

## Configuration

REST endpoint config:
```properties
ssm.rest.url=http://peer0.pr-bc1.civis-blockchain.org:9090
```

CouchDB for query optimization:
```properties
# View from infra/docker-compose/.env_dev
BCLAN_COUCH_URL=http://couchdb.bc-coop.bclan:5984
```

## Testing

Tests need Docker Compose sandbox running. `make test-pre`:
1. Pulls latest Docker images
2. Starts sandbox containers
3. Adds hosts entries for Fabric network (ca.bc-coop.bclan, peer0.bc-coop.bclan, orderer.bclan)

BDD tests use Cucumber w/ features in `ssm-bdd-features/`.