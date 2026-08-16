# fixers-c2 — Context

C2 is the **SDK for SSMs (Signing State Machines) running as Hyperledger Fabric chaincode**. It is the on-chain specialisation of the [S2 Automate](../fixers-s2/CONTEXT.md): every transition is signed by an Agent and persisted on the distributed ledger.

## Glossary

### SSM (Signing State Machine)

A specialisation of an S2 [Automate](../fixers-s2/CONTEXT.md#automate) where:

1. **State and transitions** are defined the same way as in S2 (states, transitions, roles).
2. **Persistence** is the Hyperledger Fabric ledger — the state log is a list of `SsmSessionStateLog` entries (a session state snapshot plus the `txId` of the Fabric transaction that produced it) rather than rows in MongoDB / R2DBC.
3. **Authorization** is cryptographic — every command must be signed by an [Agent](#agent) whose role is permitted by the transition.
4. **Transport** is **Chaincode**: invocations are Fabric chaincode calls, not Spring HTTP.

An SSM definition (`Ssm`: a `name` plus a list of `SsmTransition`s — each a `from`/`to` state index, a `role`, and an `action`) is registered once on the ledger via the `create` chaincode op; states are the integer indices referenced by the transitions, not a separate list. Subsequently, each running execution of the SSM is called a **Session**.

### Agent

An identity that signs SSM commands. An Agent is **on-chain only**: a `name` plus an RSA public key (`Agent(name, pub)`), registered in the SSM chaincode itself. It is *not* a Fabric MSP identity — the gateway signs the Fabric-level transaction with its own MSP identity; Agent signatures (`SHA256withRSA`) are an application-level layer on top. Two flavours (from the chaincode model):

- **User** — an agent who can trigger transitions in an SSM session (a participant in the smart contract). Registered via the `register` chaincode op.
- **Admin** — may register new users and new SSMs. Admins are declared only once, when the chaincode is instantiated.

Off-chain, the SDK holds the matching key-pairs as `SignerUser` / `SignerAdmin` (`ssm-sdk-sign-rsa-key`); anything implementing `SsmCmdSigner` can sign commands.

**Not the same as an IM User.** [connect-im](../../connect/connect-im/CONTEXT.md#user) Users live in Keycloak (off-chain identity, email/password). An IM User may *control* one or more C2 Agents, but the concepts are disjoint — do not conflate them in code, docs, or APIs.

### Session

One execution of an SSM. Has a name (`session`), a `roles` map binding each agent to an SSM role, a current state (`SsmSessionState`, with an `iteration` counter incremented at every transition), and a history of signed states (`SsmSessionStateLog`). Created via the `start` chaincode op; advanced via `perform <action>` chaincode ops, where `<action>` is the trigger named in a transition.

### SsmContext

The payload signed and sent with a `perform` op: `session`, the new `public` data, the `iteration` it applies to, and optional `private` data. The iteration counter is incremented at every transition, so a context pins a command to a specific point in the session's history.

### SsmUri

Addressing scheme for an SSM across channels and chaincodes: `ssm:<channelId>:<chaincodeId>:<ssmName>` (`SsmUri` in `ssm-chaincode-dsl`; the `channelId:chaincodeId` prefix alone is a `ChaincodeUri`). Used by the couchdb/data query layers to locate an SSM.

### Chaincode

Hyperledger Fabric's term for "smart contract bytecode running on the peers." C2 ships:

- `c2-chaincode/chaincode-ssm/` — Docker packaging of the Go SSM chaincode (deployed onto Fabric peers).
- `c2-chaincode/chaincode-api/chaincode-api-gateway/` — REST API gateway in front of the chaincode (Docker image `c2-chaincode-api-gateway`), so off-chain clients can invoke without a Fabric SDK.

### CouchDB indexing

Fabric stores world state in CouchDB. Some queries (e.g. "list all sessions where role X performed action Y") are too expensive to run on-chain. C2 offers `ssm-couchdb` (a CouchDB/Cloudant client wrapper plus F2 query functions) and `ssm-data` (combined read-side F2 query functions; `ssm-data-sync` follows the CouchDB changes feed) to query CouchDB directly — off-chain indexing of on-chain state.

### Transaction (Tx)

A Fabric blockchain transaction: `transactionId`, `blockId`, `timestamp`, `isValid`, `channelId`, `creator`, `nonce` (`Transaction` in `chaincode-dsl`). On the wire, the gateway's `POST /invoke` wraps invoke requests and their outcomes in CloudEvents 1.0 structured-mode JSON envelopes (`InvokeEnvelope`, which extends F2's `EnvelopeDTO` from `f2-dsl-cqrs`; responses carry the request `id` in `subject` for correlation).

### Sandbox

A pre-configured Hyperledger Fabric network used for local testing of SSM chaincode (`c2-sandbox/`). Not for production.

## Module map

- **`c2-chaincode/`** — blockchain side: `chaincode-dsl`, `chaincode-api/` (`chaincode-api-config`, `chaincode-api-fabric`, `chaincode-api-gateway`), `chaincode-ssm` (Go chaincode packaging), `chaincode-ex02` (example chaincode).
- **`c2-ssm/`** — Kotlin SDK (off-chain client side): `ssm-sdk` (incl. `ssm-sdk-json`, `ssm-sdk-sign`, `ssm-sdk-sign-rsa-key`), `ssm-chaincode`, `ssm-couchdb`, `ssm-data`, `ssm-tx`, `ssm-s2` (S2 `Automate` -> `Ssm` bridge), `ssm-spring`, `ssm-bdd`.
- **`c2-sandbox/`** — local Fabric network for tests.
- **`sample/`** — orderbook (sourcing/storing) and DID sample apps.

## Cross-references

- Specialisation of: [../fixers-s2/CONTEXT.md](../fixers-s2/CONTEXT.md) (Automate, State, Transition, Role) — code-level: `ssm-s2-dsl` converts an `S2Automate` to an `Ssm` (`S2Automate.toSsm()`), and `ssm-spring` ships S2 sourcing/storing starters backed by SSM.
- Uses: [../fixers-f2/CONTEXT.md](../fixers-f2/CONTEXT.md) (Command/Query/Event via `f2-dsl-cqrs`; `f2-spring-boot-starter-function` for the gateway).
- Layer position: [../../docs/adr/0001-submodule-dependency-layers.md](../../docs/adr/0001-submodule-dependency-layers.md).
