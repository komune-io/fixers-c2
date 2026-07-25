package io.komune.c2.chaincode.api.fabric.autoconfiguration

import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "coop.fabric")
class FabricClientProperties {
    /**
     * Max concurrent gateway operations — both `query`/evaluate calls and invoke endorse+submit
     * calls — backing the `Dispatchers.IO.limitedParallelism(...)` view in [FabricGatewayClient].
     * Raising it lets a single gateway keep more transactions in flight against the peer; the
     * practical ceiling is the peer's `CORE_PEER_LIMITS_CONCURRENCY_GATEWAYSERVICE` (default 500)
     * and, beyond that, the orderer/peer throughput itself. Must be >= 1.
     */
    @Suppress("MagicNumber")
    var parallelism: Int = 256
        set(value) {
            require(value >= 1) { "coop.fabric.parallelism must be >= 1, was $value" }
            field = value
        }
}
