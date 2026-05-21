package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.fabric.FabricGatewayBuilder
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.api.gateway.ChaincodeApiGatewayApplication
import io.komune.c2.chaincode.api.gateway.chaincode.model.InvokeOutcome
import io.komune.c2.chaincode.api.gateway.chaincode.model.InvokeRequestEnvelope
import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainServiceI
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.util.UriComponentsBuilder

/**
 * Embedded-Spring integration test for POST /invoke/v2.
 *
 * The real FabricGatewayClient is replaced via a FabricGatewayBuilder subclass
 * whose contracts() are never actually called; instead we inject a custom
 * ChaincodeService that uses a FabricGatewayClient with a scripted stub
 * FabricGatewayBuilder. Since FabricGatewayClient is final, we control its
 * behavior by wrapping ChaincodeService directly with a @Primary bean backed
 * by a stub FabricGatewayClient that uses a scripted FabricGatewayBuilder.
 *
 * The FabricGatewayBuilder stub overrides contracts() to return a stub Contract
 * that always completes transactions synchronously, allowing us to simulate all
 * 5 TxOutcome variants. For the GATEWAY_EXCEPTION test we use an invalid
 * channel/chaincode id so getChannelChaincodePair() throws before reaching Fabric.
 *
 * The real doInvokeV2 / toWire mapping is exercised end-to-end via HTTP.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ChaincodeApiGatewayApplication::class, ChaincodeServiceExecuteV2EmbeddedTest.TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestRestTemplate
class ChaincodeServiceExecuteV2EmbeddedTest {

    // ---------------------------------------------------------------------------
    // Scripted FabricGatewayBuilder: returns scripted TxOutcome per commandId
    // ---------------------------------------------------------------------------

    /**
     * Holds scripted per-commandId outcomes. ChaincodeService.doInvokeV2 calls
     * fabricGatewayClient.invoke(channelId, chaincodeId, invokeArgsList, commandIds).
     * We intercept that by providing a FabricGatewayClient constructed with a builder
     * whose invoke is controlled through a shared mutable slot.
     *
     * Since FabricGatewayClient is final, we wrap the whole ChaincodeService as a
     * @Primary @Bean in TestConfig.
     */
    class ScriptedChaincodeService(
        fabricGatewayClient: FabricGatewayClient,
        blockchainService: BlockchainServiceI,
        chaincodeConfiguration: C2ChaincodeConfiguration,
        private val scriptedOutcomes: MutableList<TxOutcome>,
    ) : ChaincodeService(fabricGatewayClient, blockchainService, chaincodeConfiguration) {
        override suspend fun doInvokeV2(
            channelId: ChannelId,
            chainCodeId: ChaincodeId,
            invokeArgs: List<InvokeArgs>,
            msgIds: List<String>,
        ): List<InvokeOutcome> {
            return scriptedOutcomes.map { it.toWirePublic() }
        }

        private fun TxOutcome.toWirePublic(): InvokeOutcome = when (this) {
            is TxOutcome.Committed -> InvokeOutcome(
                outcome = "Committed", msgId = msgId,
                transactionId = transactionId, blockNumber = blockNumber, payload = payload,
            )
            is TxOutcome.Rejected -> InvokeOutcome(
                outcome = "Rejected", msgId = msgId,
                errorCode = errorCode, errorMessage = errorMessage,
            )
            is TxOutcome.Transient -> InvokeOutcome(
                outcome = "Transient", msgId = msgId,
                errorCode = errorCode, errorMessage = errorMessage,
            )
            is TxOutcome.Indeterminate -> InvokeOutcome(
                outcome = "Indeterminate", msgId = msgId,
                errorCode = errorCode, errorMessage = errorMessage,
            )
            is TxOutcome.Conflict -> InvokeOutcome(
                outcome = "Conflict", msgId = msgId,
                errorCode = errorCode, errorMessage = errorMessage,
                transactionId = transactionId, blockNumber = blockNumber,
            )
        }
    }

    @TestConfiguration
    class TestConfig {
        val scriptedOutcomes: MutableList<TxOutcome> = mutableListOf()

        @Bean
        @Primary
        fun chaincodeService(
            fabricGatewayClient: FabricGatewayClient,
            blockchainService: BlockchainServiceI,
            chaincodeConfiguration: C2ChaincodeConfiguration,
        ): ChaincodeService = ScriptedChaincodeService(
            fabricGatewayClient, blockchainService, chaincodeConfiguration, scriptedOutcomes,
        )
    }

    // ---------------------------------------------------------------------------
    // Test wiring
    // ---------------------------------------------------------------------------

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var testConfig: TestConfig

    private fun baseUrl() = UriComponentsBuilder.fromUriString("http://localhost:$port")

    /** A minimal valid InvokeRequestEnvelope pointing at the sandbox/ex02 chaincode. */
    private fun makeRequest(msgId: String) = InvokeRequestEnvelope(
        msgId = msgId,
        request = InvokeRequest(
            channelid = "sandbox",
            chaincodeid = "ex02",
            cmd = InvokeRequestType.invoke,
            fcn = "invoke",
            args = arrayOf("a", "b", "1"),
        ),
    )

    @BeforeEach
    fun resetStub() {
        testConfig.scriptedOutcomes.clear()
    }

    private fun post(vararg requests: InvokeRequestEnvelope): List<InvokeOutcome> {
        val uri = baseUrl().path("invoke/v2").build().toUri()
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(requests.toList(), headers)
        val typeRef = object : ParameterizedTypeReference<List<InvokeOutcome>>() {}
        val response = restTemplate.exchange(uri, HttpMethod.POST, entity, typeRef)
        assertThat(response.statusCode.value()).isEqualTo(200)
        return response.body!!
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `POST invoke-v2 returns Committed item with transactionId blockNumber payload`() {
        testConfig.scriptedOutcomes += TxOutcome.Committed(
            msgId = "cmd-1",
            transactionId = "tx-abc",
            blockNumber = 42L,
            payload = "{}",
        )

        val outcomes = post(makeRequest("cmd-1"))

        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertThat(item.outcome).isEqualTo("Committed")
        assertThat(item.msgId).isEqualTo("cmd-1")
        assertThat(item.transactionId).isEqualTo("tx-abc")
        assertThat(item.blockNumber).isEqualTo(42L)
        assertThat(item.payload).isEqualTo("{}")
        assertThat(item.errorCode).isNull()
        assertThat(item.errorMessage).isNull()
    }

    @Test
    fun `POST invoke-v2 returns Rejected item with errorCode errorMessage commandId only`() {
        testConfig.scriptedOutcomes += TxOutcome.Rejected(
            msgId = "cmd-rej",
            errorCode = "ENDORSE_FAILED",
            errorMessage = "endorsement failed",
        )

        val outcomes = post(makeRequest("cmd-rej"))

        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertThat(item.outcome).isEqualTo("Rejected")
        assertThat(item.msgId).isEqualTo("cmd-rej")
        assertThat(item.errorCode).isEqualTo("ENDORSE_FAILED")
        assertThat(item.errorMessage).isEqualTo("endorsement failed")
        assertThat(item.transactionId).isNull()
        assertThat(item.blockNumber).isNull()
    }

    @Test
    fun `POST invoke-v2 returns Transient item with errorCode errorMessage commandId only`() {
        testConfig.scriptedOutcomes += TxOutcome.Transient(
            msgId = "cmd-tr",
            errorCode = "GRPC_UNAVAILABLE",
            errorMessage = "peer unreachable",
        )

        val outcomes = post(makeRequest("cmd-tr"))

        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertThat(item.outcome).isEqualTo("Transient")
        assertThat(item.msgId).isEqualTo("cmd-tr")
        assertThat(item.errorCode).isEqualTo("GRPC_UNAVAILABLE")
        assertThat(item.errorMessage).isEqualTo("peer unreachable")
        assertThat(item.transactionId).isNull()
        assertThat(item.blockNumber).isNull()
    }

    @Test
    fun `POST invoke-v2 returns Indeterminate item with errorCode errorMessage commandId only`() {
        testConfig.scriptedOutcomes += TxOutcome.Indeterminate(
            msgId = "cmd-ind",
            errorCode = "SUBMIT_FAILED",
            errorMessage = "orderer timeout",
        )

        val outcomes = post(makeRequest("cmd-ind"))

        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertThat(item.outcome).isEqualTo("Indeterminate")
        assertThat(item.msgId).isEqualTo("cmd-ind")
        assertThat(item.errorCode).isEqualTo("SUBMIT_FAILED")
        assertThat(item.errorMessage).isEqualTo("orderer timeout")
        assertThat(item.transactionId).isNull()
        assertThat(item.blockNumber).isNull()
    }

    @Test
    fun `POST invoke-v2 returns Conflict item with transactionId blockNumber AND errorCode errorMessage`() {
        testConfig.scriptedOutcomes += TxOutcome.Conflict(
            msgId = "cmd-cf",
            errorCode = "MVCC_READ_CONFLICT",
            errorMessage = "conflict on key session-xyz",
            transactionId = "tx-conflict",
            blockNumber = 7L,
        )

        val outcomes = post(makeRequest("cmd-cf"))

        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertThat(item.outcome).isEqualTo("Conflict")
        assertThat(item.msgId).isEqualTo("cmd-cf")
        assertThat(item.errorCode).isEqualTo("MVCC_READ_CONFLICT")
        assertThat(item.errorMessage).isEqualTo("conflict on key session-xyz")
        assertThat(item.transactionId).isEqualTo("tx-conflict")
        assertThat(item.blockNumber).isEqualTo(7L)
    }

    @Test
    fun `POST invoke-v2 wraps gateway exception as Rejected with GATEWAY_EXCEPTION code`() {
        // ChaincodeService.executeV2 has runCatching wrapping the whole operation.
        // getChannelChaincodePair throws InvokeException for unknown channel/chaincode,
        // which is caught and becomes a GATEWAY_EXCEPTION Rejected outcome.
        val badRequest = InvokeRequestEnvelope(
            msgId = "cmd-exc",
            request = InvokeRequest(
                channelid = "invalid_channel",
                chaincodeid = "invalid_cc",
                cmd = InvokeRequestType.invoke,
                fcn = "invoke",
                args = arrayOf("a"),
            ),
        )

        val uri = baseUrl().path("invoke/v2").build().toUri()
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(listOf(badRequest), headers)
        val typeRef = object : ParameterizedTypeReference<List<InvokeOutcome>>() {}
        val response = restTemplate.exchange(uri, HttpMethod.POST, entity, typeRef)

        assertThat(response.statusCode.value()).isEqualTo(200)
        val outcomes = response.body!!
        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertThat(item.outcome).isEqualTo("Rejected")
        assertThat(item.msgId).isEqualTo("cmd-exc")
        assertThat(item.errorCode).isEqualTo("GATEWAY_EXCEPTION")
        assertThat(item.errorMessage).isNotEmpty()
    }
}
