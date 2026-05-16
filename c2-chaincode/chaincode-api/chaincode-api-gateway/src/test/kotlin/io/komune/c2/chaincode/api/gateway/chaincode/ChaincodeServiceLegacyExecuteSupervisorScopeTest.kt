package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.fabric.FabricGatewayBuilder
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainServiceI
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode

/**
 * Unit tests (no Spring) pinning the supervisorScope behavior introduced in Plan A
 * commit cc1e6cc4 for ChaincodeService.execute(List<InvokeRequest>).
 *
 * With supervisorScope: a failing child does NOT cancel its siblings — they all
 * run to completion even though awaitAll() eventually rethrows the first failure.
 *
 * The previous coroutineScope behavior would cancel siblings on first failure.
 *
 * Strategy:
 *   - Item 1 of 3 has an invalid channelid/chaincodeid → getChannelChaincodePair
 *     throws InvokeException before reaching the FabricGatewayClient.
 *   - Items 0 and 2 have valid ccids → their async blocks reach doInvoke and
 *     call FabricGatewayClient.invoke (counted via side-effect counter).
 *   - awaitAll() rethrows item 1's failure. We catch it and assert counter == 2.
 */
class ChaincodeServiceLegacyExecuteSupervisorScopeTest {

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private var invokeCallCount = 0

    /** Counts each call to invoke() and returns a list of one TxOutcome.Committed. */
    private fun countingStubBuilder(): FabricGatewayBuilder =
        object : FabricGatewayBuilder() {
            override fun contracts(channelId: ChannelId, chaincodeId: io.komune.c2.chaincode.dsl.ChaincodeId) =
                error("stub: contracts() should not be called directly")
        }

    /**
     * Build a FabricGatewayClient whose invoke() increments the counter and returns one
     * TxOutcome.Committed so that doInvoke() succeeds for valid items.
     *
     * Because FabricGatewayClient is final (no Spring allopen in unit scope), we create
     * it with the counting stub builder and override its invoke via a counting subclass-
     * equivalent. Since we cannot subclass final FabricGatewayClient here, we instead
     * subclass ChaincodeService and override doInvoke() to increment the counter.
     */
    private fun buildCountingService(chaincodeConfiguration: C2ChaincodeConfiguration): ChaincodeService {
        // Provide a no-op FabricGatewayClient backed by a dummy builder. The client
        // is never actually called because doInvoke is overridden in the subclass.
        val dummyBuilder = object : FabricGatewayBuilder() {
            override fun contracts(channelId: ChannelId, chaincodeId: io.komune.c2.chaincode.dsl.ChaincodeId) =
                emptyList<org.hyperledger.fabric.client.Contract>()
        }
        val dummyFabricClient = FabricGatewayClient(dummyBuilder)

        val dummyBlockchainService = object : BlockchainServiceI {
            override fun query(channelId: ChannelId, invokeArgs: InvokeArgs): String = ""
            override fun queryAllBlocks(channelId: ChannelId): String = ""
            override fun queryBlockByNumber(channelId: ChannelId, invokeArgs: InvokeArgs): String = ""
            override fun queryAllTransactions(channelId: ChannelId): String = ""
            override fun queryTransactionById(channelId: ChannelId, invokeArgs: InvokeArgs): String = ""
        }

        // ChaincodeService is open (kotlin.spring allopen plugin applied in production),
        // but in unit tests the allopen plugin still runs because it is applied via
        // the Gradle plugin to ALL compilations (test sources included).
        return object : ChaincodeService(dummyFabricClient, dummyBlockchainService, chaincodeConfiguration) {
            override suspend fun doInvoke(
                channelId: ChannelId,
                chainCodeId: io.komune.c2.chaincode.dsl.ChaincodeId,
                invokeArgs: List<InvokeArgs>,
            ): List<io.komune.c2.chaincode.dsl.invoke.InvokeReturn> {
                invokeCallCount++
                return invokeArgs.map {
                    io.komune.c2.chaincode.dsl.invoke.InvokeReturn(status = "SUCCESS", info = "", transactionId = "tx-stub")
                }
            }
        }
    }

    /**
     * Build a C2ChaincodeConfiguration that accepts "sandbox/ex02" and "sandbox/ssm"
     * as valid ccids (matching the test application.yml). An invalid ccid causes
     * getChannelChaincodePair() to throw InvokeException — which is the failure we inject.
     */
    private fun buildConfig(): C2ChaincodeConfiguration {
        val config = C2ChaincodeConfiguration(
            defaultCcid = "sandbox/ex02",
            ccid = "sandbox/ssm, sandbox/ex02",
            user = C2ChaincodeConfiguration.UserConfig().apply {
                name = "test-user"
                password = "test-pass"
                org = "bclan"
            },
            endorsers = "peer0:bclan",
        )
        return config
    }

    private fun validRequest(channelid: String, chaincodeid: String) = InvokeRequest(
        channelid = channelid,
        chaincodeid = chaincodeid,
        cmd = InvokeRequestType.invoke,
        fcn = "invoke",
        args = arrayOf("a", "b", "1"),
    )

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `legacy execute uses supervisorScope so siblings are NOT cancelled on first failure`() = runTest {
        invokeCallCount = 0
        val service = buildCountingService(buildConfig())

        // Item 0: valid → will call doInvoke
        // Item 1: invalid ccid → getChannelChaincodePair throws InvokeException
        // Item 2: valid → will call doInvoke
        val requests = listOf(
            validRequest("sandbox", "ex02"),
            validRequest("INVALID_CHANNEL", "INVALID_CC"),
            validRequest("sandbox", "ssm"),
        )

        val thrown = runCatching { service.execute(requests) }.exceptionOrNull()

        // supervisorScope: siblings are not cancelled, so both valid items call doInvoke
        assertThat(invokeCallCount).isEqualTo(2)
        // awaitAll() still rethrows the failure from item 1
        assertThat(thrown).isNotNull()
    }

    @Test
    fun `legacy execute returns all results when no items fail`() = runTest {
        invokeCallCount = 0
        val service = buildCountingService(buildConfig())

        val requests = listOf(
            validRequest("sandbox", "ex02"),
            validRequest("sandbox", "ssm"),
        )

        val results: List<JsonNode> = service.execute(requests)

        assertThat(invokeCallCount).isEqualTo(2)
        assertThat(results).hasSize(2)
    }
}
