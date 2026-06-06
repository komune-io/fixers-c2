# fixers-c2 — Context

C2 is the **SDK for SSMs (Signing State Machines) running as Hyperledger Fabric chaincode**. It is the on-chain specialisation of the [S2 Automate](../fixers-s2/CONTEXT.md): every transition is signed by an Agent and persisted on the distributed ledger.

## Glossary

### SSM (Signing State Machine)

A specialisation of an S2 [Automate](../fixers-s2/CONTEXT.md#automate) where:

1. **State and transitions** are defined the same way as in S2 (states, transitions, roles).
2. **Persistence** is the Hyperledger Fabric ledger — the state log lives in `SsmSessionStateLog` blocks rather than in MongoDB / R2DBC.
3. **Authorization** is cryptographic — every command must be signed by an [Agent](#agent) whose role is permitted by the transition.
4. **Transport** is **Chaincode**: invocations are Fabric chaincode calls, not Spring HTTP.

An SSM definition (states, transitions, roles, allowed commands) is registered once on the ledger; subsequently, each running execution of the SSM is called a **Session**.

### Agent

An identity that signs SSM transactions. An Agent is **on-chain only**: a key-pair (public + private), bound to a Fabric MSP (Membership Service Provider). Two flavours:

- **User agent** — a participant in an SSM session (initiates / accepts transitions).
- **Admin agent** — governance role (registers SSMs, manages the network).

**Not the same as an IM User.** [connect-im](../../connect/connect-im/CONTEXT.md#user) Users live in Keycloak (off-chain identity, email/password). An IM User may *control* one or more C2 Agents, but the concepts are disjoint — do not conflate them in code, docs, or APIs.

### Session

One execution of an SSM. Has an id, a current state, a history of signed transitions (`SsmSessionStateLog`). Created via `start session` chaincode op; advanced via `perform <transition>` chaincode ops.

### Chaincode

Hyperledger Fabric's term for "smart contract bytecode running on the peers." C2 ships:

- `chaincode-ssm/` — the SSM chaincode implementation itself (deployed onto Fabric peers).
- `chaincode-api/gateway/` — REST API gateway in front of the chaincode (Docker image `c2-chaincode-api-gateway`), so off-chain clients can invoke without a Fabric SDK.

### CouchDB indexing

Fabric stores world state in CouchDB. Some queries (e.g. "list all sessions where role X performed action Y") are too expensive to run on-chain. C2 offers `ssm-couchdb` and the combined `ssm-data` artifacts to query CouchDB directly — off-chain indexing of on-chain state.

### Transaction (Tx)

A Fabric blockchain transaction: `transactionId`, `blockNumber`, signed payload. C2 wraps these in CloudEvents 1.0 JSON envelopes on the wire so the same machinery (`f2-spring`, `f2-dsl-cqrs`) that handles HTTP F2 functions can also handle SSM events.

### Sandbox

A pre-configured Hyperledger Fabric network used for local testing of SSM chaincode (`c2-sandbox/`). Not for production.

## Module map

- **`c2-chaincode/`** — chaincode + gateway (blockchain side).
- **`c2-ssm/`** — Kotlin SDK (off-chain client side): `ssm-sdk`, `ssm-chaincode`, `ssm-couchdb`, `ssm-data`, `ssm-tx`, `ssm-spring`, `ssm-bdd`.
- **`c2-sandbox/`** — local Fabric network for tests.

## Cross-references

- Specialisation of: [../fixers-s2/CONTEXT.md](../fixers-s2/CONTEXT.md) (Automate, State, Transition, Role).
- Uses: [../fixers-f2/CONTEXT.md](../fixers-f2/CONTEXT.md) (Command/Query/Event via `f2-dsl-cqrs`; `f2-spring-boot-starter-function` for the gateway).
- Layer position: [../../docs/adr/0001-submodule-dependency-layers.md](../../docs/adr/0001-submodule-dependency-layers.md).
