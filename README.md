# fixers-c2

Kotlin SDK for interacting with [Blockchain-SSM](https://github.com/civis-blockchain/blockchain-ssm) (Signing State Machines) chaincode on Hyperledger Fabric.

## Quick Start

### Using Spring Boot Starters (Recommended)

```kotlin
// Spring Boot auto-configuration for chaincode operations
implementation("io.komune.c2:ssm-chaincode-spring-boot-starter:${Versions.c2}")

// Spring Boot auto-configuration for CouchDB queries
implementation("io.komune.c2:ssm-couchdb-spring-boot-starter:${Versions.c2}")

// Spring Boot auto-configuration for combined data queries
implementation("io.komune.c2:ssm-data-spring-boot-starter:${Versions.c2}")
```

### Configuration

```properties
ssm.rest.url=http://peer0.pr-bc1.civis-blockchain.org:9090
```

## Libraries

### SSM-Chaincode

Communicates directly with the blockchain API for queries and transactions over Signing State Machines.

```kotlin
// Data models and query function definitions
implementation("io.komune.c2:ssm-chaincode-dsl:${Versions.c2}")

// F2 function implementations
implementation("io.komune.c2:ssm-chaincode-f2:${Versions.c2}")
```

### SSM-CouchDB

Queries CouchDB attached to the blockchain to optimize costly requests (listing SSMs, sessions).

```kotlin
// Data models and query function definitions
implementation("io.komune.c2:ssm-couchdb-dsl:${Versions.c2}")

// F2 function implementations
implementation("io.komune.c2:ssm-couchdb-f2:${Versions.c2}")
```

### SSM-Data

Combines chaincode and CouchDB modules for complex queries.

```kotlin
// Data models and query function definitions
implementation("io.komune.c2:ssm-data-dsl:${Versions.c2}")

// F2 function implementations
implementation("io.komune.c2:ssm-data-f2:${Versions.c2}")
```

### SSM-TX

Transaction management for SSM operations.

```kotlin
// Data models
implementation("io.komune.c2:ssm-tx-dsl:${Versions.c2}")

// F2 function implementations
implementation("io.komune.c2:ssm-tx-f2:${Versions.c2}")
```

> All F2 functions are implemented as Spring Beans for dependency injection.

## Development

### Build Commands

```bash
make lint      # Run detekt static analysis
make build     # Build and publish to Maven local
make test      # Run all tests (requires dev environment)
```

### Development Environment

A pre-configured Hyperledger Fabric network is provided via Docker Compose:

```bash
make dev up    # Start sandbox services
make dev down  # Stop sandbox services
make dev logs  # Follow service logs
```

Before running tests:
```bash
make test-pre  # Pull images, start containers, configure /etc/hosts
```

## Infrastructure

### SSM-Sandbox

Pre-configured Hyperledger Fabric Network with generated cryptographic materials and SSM chaincode installed. See [c2-sandbox/README.md](c2-sandbox/README.md) for details.

### Chaincode API Gateway

REST API gateway for blockchain operations. Builds as Docker image `c2-chaincode-api-gateway`.
