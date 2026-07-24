package io.komune.c2.chaincode.api.fabric.autoconfiguration

import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "coop.fabric")
class FabricClientProperties {
    /**
     * Max concurrent endorse+submit calls the gateway client dispatches, backing the
     * `Dispatchers.IO.limitedParallelism(...)` view in [FabricGatewayClient]. Raising it lets a
     * single gateway keep more transactions in flight against the peer; the practical ceiling is
     * the peer's `CORE_PEER_LIMITS_CONCURRENCY_GATEWAYSERVICE` (default 500) and, beyond that, the
     * orderer/peer throughput itself.
     */
    @Suppress("MagicNumber")
    var parallelism: Int = 256
}
