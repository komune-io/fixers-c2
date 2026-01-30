# Introduction

This SDK is a Kotlin library used to interact with a [Blockchain-SSM](https://github.com/civis-blockchain/blockchain-ssm) chaincode hosted with Hyperledger Fabric.

# Infra

## SSM-Chaincode

[SSM-Chaincode](/docs/chaincode-dsl-signing-state-machine--page) communicates directly with the blockchain API and is able to make simple queries and transactions over Signing State Machines.

* Build
```
make ssm-chaincode-package
```

## SSM-Sandbox

This setup provides a Hyperledger Fabric Network pre-configured with generated cryptographic materials
and the SSM chaincode installed, facilitating a quick start for development and testing purposes.

* Build
```
make ssm-chaincode-package
```

# Lib 

## SSM-Chaincode
[SSM-Chaincode](/docs/chaincode-dsl-signing-state-machine--page) communicates directly with the blockchain API and is able to make simple queries and transactions over Signing State Machines.

**Import with Gradle**
```kotlin
// Data models and query function definitions
implementation("io.komune.c2:ssm-chaincode-dsl:${Versions.ssm}")

// Implementation of query functions defined in ssm-chaincode-dsl
implementation("io.komune.c2:f2-query:${Versions.ssm}")

// Function to create an SSM
implementation("io.komune.c2:f2-create-ssm:${Versions.ssm}")

// Function to start a session on a given SSM
implementation("io.komune.c2:f2-session-start:${Versions.ssm}")

// Function to perform an action on a given session
implementation("io.komune.c2:f2-session-perform-action:${Versions.ssm}")
```
> Note: All functions are implemented as Spring Beans, so they can be instantiated with Dependency Injection mechanism

## SSM-CouchDB

[SSM-CouchDB](/docs/ssm-couchdb-general--page) is able to query a CouchDB attached to a blockchain in order to optimize costly requests.

**Import with Gradle**
```kotlin
// Data models and query function definitions
implementation("io.komune.c2:ssm-couchdb-dsl:${Versions.ssm}")

// Implementation of query functions defined in ssm-couchdb-dsl
implementation("io.komune.c2:ssm-couchdb-f2:${Versions.ssm}")
```
> Note: All functions are implemented as Spring Beans, so they can be instantiated with Dependency Injection mechanism

## SSM-Data

[SSM-Data](/docs/ssm-tx-general--page) uses the two previous modules to provide more complex and detailed queries.

**Import with Gradle**
```kotlin
// Data models and query function definitions
implementation("io.komune.c2:ssm-data-dsl:${Versions.ssm}")

// Implementation of query functions defined in ssm-couchdb-dsl
implementation("io.komune.c2:ssm-data-f2:${Versions.ssm}")
```
> Note: All functions are implemented as Spring Beans, so they can be instantiated with Dependency Injection mechanism
