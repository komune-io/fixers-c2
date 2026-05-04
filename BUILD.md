# fixers-c2 Build Guide

## Dependency Graph

```
make build
│
├─ libs.mk build ─────────────────────────────────────────────────────────────
│  │
│  └─ ./gradlew clean build publishToMavenLocal -x test
│     │
│     │  31 Gradle subprojects (parallel)
│     │
│     ├─ c2-chaincode
│     │  ├─ chaincode-api-fabric         Fabric SDK integration
│     │  └─ chaincode-api-gateway        Spring Boot REST/gRPC app
│     │
│     ├─ c2-ssm/ssm-sdk
│     │  ├─ ssm-sdk-dsl                  DSL (Kotlin MPP: JVM + JS)
│     │  ├─ ssm-sdk-json                 Jackson serialization
│     │  ├─ ssm-sdk-sign                 RSA signing
│     │  ├─ ssm-sdk-sign-rsa-key         RSA key management
│     │  ├─ ssm-sdk-core ◄──────────────── depends on all above + ssm-chaincode-dsl
│     │  └─ ssm-sdk-bdd                  BDD test support
│     │
│     ├─ c2-ssm/ssm-chaincode
│     │  ├─ ssm-chaincode-dsl            DSL (Kotlin MPP)
│     │  ├─ ssm-chaincode-f2 ◄────────── depends on ssm-chaincode-dsl, ssm-sdk-core
│     │  ├─ ssm-chaincode-f2-client
│     │  └─ ssm-chaincode-bdd
│     │
│     ├─ c2-ssm/ssm-couchdb
│     │  ├─ ssm-couchdb-dsl ◄────────── depends on ssm-chaincode-dsl
│     │  ├─ ssm-couchdb-sdk
│     │  ├─ ssm-couchdb-f2
│     │  └─ ssm-couchdb-bdd
│     │
│     ├─ c2-ssm/ssm-data
│     │  ├─ ssm-data-dsl ◄──────────── depends on ssm-couchdb-dsl, ssm-chaincode-dsl
│     │  ├─ ssm-data-f2
│     │  ├─ ssm-data-sync
│     │  └─ ssm-data-bdd
│     │
│     ├─ c2-ssm/ssm-tx
│     │  ├─ ssm-tx-dsl
│     │  ├─ ssm-tx-f2
│     │  └─ ssm-tx-bdd
│     │
│     └─ c2-ssm/ssm-spring                Spring Boot starters
│        ├─ ssm-chaincode-spring-boot-starter
│        ├─ ssm-couchdb-spring-boot-starter
│        ├─ ssm-data-spring-boot-starter
│        └─ ssm-tx-spring-boot-starter
│           ├─ ssm-tx-config-spring-boot-starter
│           ├─ ssm-tx-create-ssm-spring-boot-starter
│           ├─ ssm-tx-init-ssm-spring-boot-starter
│           ├─ ssm-tx-session-perform-action-spring-boot-starter
│           └─ ssm-tx-session-start-spring-boot-starter
│
│
├─ docker.mk build ───────────────────────────────────────────────────────────
│  │
│  │  PHASE 1 — Gateway image (from Gradle)
│  │
│  ├─ [1] docker-chaincode-api-gateway-build
│  │      ./gradlew bootBuildImage → c2-chaincode-api-gateway:VERSION (local)
│  │      Base: Paketo buildpacks (implicit)
│  │
│  │  PHASE 2 — Go chaincodes (parallel-safe, no inter-deps)
│  │
│  ├─ [2a] c2-chaincode/chaincode-ex02
│  │       Dockerfile: c2-chaincode/chaincode-ex02/Dockerfile
│  │       FROM hyperledger/fabric-tools:2.5.7 (builder)
│  │       FROM alpine:3.19 (runtime)
│  │       Produces: c2-chaincode-ex02:VERSION (local)
│  │
│  ├─ [2b] c2-chaincode/chaincode-ssm
│  │       Dockerfile: c2-chaincode/chaincode-ssm/Dockerfile
│  │       FROM alpine:3.19 (fetcher — downloads github.com/apoupard/blockchain-ssm)
│  │       FROM hyperledger/fabric-tools:2.5.7 (builder)
│  │       FROM alpine:3.19 (runtime)
│  │       Produces: c2-chaincode-ssm:VERSION (local)
│  │
│  │  PHASE 3 — Sandbox containers
│  │
│  ├─ [3a] c2-sandbox-ca
│  │       Dockerfile: c2-sandbox/c2-sandbox-ca/Dockerfile
│  │       FROM hyperledger/fabric-ca:1.5.10
│  │       Produces: c2-sandbox-ca:VERSION (local)
│  │
│  ├─ [3b] c2-sandbox-orderer
│  │       Dockerfile: c2-sandbox/c2-sandbox-orderer/Dockerfile
│  │       FROM hyperledger/fabric-orderer:2.5.7
│  │       Produces: c2-sandbox-orderer:VERSION (local)
│  │
│  ├─ [3c] c2-sandbox-peer
│  │       Dockerfile: c2-sandbox/c2-sandbox-peer/Dockerfile
│  │       FROM hyperledger/fabric-peer:2.5.7
│  │       Produces: c2-sandbox-peer:VERSION (local)
│  │
│  ├─ [3d] c2-sandbox-cli ⚠️ depends on [2a] + [2b]
│  │       Dockerfile: c2-sandbox/c2-sandbox-cli/Dockerfile
│  │       FROM hyperledger/fabric-tools:2.5.7
│  │       COPY --from=c2-chaincode-ssm:VERSION   ← local image
│  │       COPY --from=c2-chaincode-ex02:VERSION   ← local image
│  │       Produces: c2-sandbox-cli:VERSION (local)
│  │
│  └─ [3e] c2-sandbox-ssm-gateway ⚠️ depends on [1]
│          Dockerfile: c2-sandbox/c2-sandbox-ssm-gateway/Dockerfile
│          FROM c2-chaincode-api-gateway:VERSION   ← local image
│          Produces: c2-sandbox-ssm-gateway:VERSION (local)
```

## Hard Ordering Constraints

```
Gradle JVM build ──► [1] bootBuildImage ──► [3e] ssm-gateway
                                               (FROM c2-chaincode-api-gateway)

docker.mk ──► [2a] chaincode-ex02 ──► [3d] cli
              [2b] chaincode-ssm  ──►      (COPY --from chaincodes)
```

- `c2-sandbox-ssm-gateway` uses the locally-built `c2-chaincode-api-gateway:VERSION` as its base image. `DOCKER_REPOSITORY` must be empty during build.
- `c2-sandbox-cli` uses `COPY --from=c2-chaincode-ssm:VERSION` and `COPY --from=c2-chaincode-ex02:VERSION`. These chaincodes must be built first.
- All other sandbox images (ca, orderer, peer) use external base images and can build in any order.

## Make Targets

| Target | libs.mk (Gradle) | docker.mk (Docker) |
|--------|-------------------|---------------------|
| `build` | `publishToMavenLocal -x test` | Build all images locally |
| `test` | `./gradlew test` | (none) |
| `stage` | `./gradlew stage` | Tag + push to `ghcr.io/komune-io/` |
| `promote` | `./gradlew promote` | Tag + push to `docker.io/komune/` |

## Environment Variables

From `.env_version`:

| Variable | Value | Used by |
|----------|-------|---------|
| `VERSION_ALPINE` | 3.19.1 | chaincode Dockerfiles |
| `VERSION_FABRIC` | 2.5.7 | peer, orderer, cli, chaincode builders |
| `VERSION_FABRIC_CA` | 1.5.10 | CA |
| `SSM_VERSION` | 2.5.7-0.8.2 | chaincode-ssm |

From `VERSION` file:

| Variable | Source | Used by |
|----------|--------|---------|
| `VERSION` | `$(shell cat VERSION)` | All Makefiles, Gradle, Docker tags |

From release orchestration:

| Variable | Purpose |
|----------|---------|
| `MAVEN_LOCAL_USE=true` | Enables `mavenLocal()` in `settings.gradle.kts` |
| `DOCKER_REPOSITORY` | Registry prefix — empty for build, set for stage/promote |

## External Dependencies

| Image | Version | Purpose |
|-------|---------|---------|
| `hyperledger/fabric-tools` | 2.5.7 | Go chaincode compilation |
| `hyperledger/fabric-peer` | 2.5.7 | Blockchain peer node |
| `hyperledger/fabric-orderer` | 2.5.7 | Blockchain orderer node |
| `hyperledger/fabric-ca` | 1.5.10 | Certificate authority |
| `alpine` | 3.19 | Lightweight runtime for chaincodes |
| Paketo buildpacks | (implicit) | Spring Boot image for chaincode-api-gateway |