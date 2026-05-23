package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.api.gateway.ChaincodeApiGatewayApplication
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainServiceI
import io.komune.c2.chaincode.api.gateway.chaincode.model.OutcomeData
import io.komune.c2.chaincode.api.gateway.config.CloudEventsProperties
import io.komune.c2.chaincode.dsl.cloudevent.InvokeEnvelope
import io.komune.c2.chaincode.dsl.cloudevent.InvokeType
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.util.UriComponentsBuilder

/**
 * Embedded-Spring integration test for `POST /invoke` (CloudEvents 1.0 structured-mode batch).
 *
 * Strategy: a [ScriptedChaincodeService] subclass overrides the protected `runInvoke` hook
 * to return scripted (type, OutcomeData) pairs computed via the production [toWire] mapping.
 * This exercises the real CloudEvents envelope construction (id, source, subject, time)
 * end-to-end over HTTP without needing a live Fabric backend. All 5 [TxOutcome] variants
 * plus the gateway-exception path are covered.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ChaincodeApiGatewayApplication::class, ChaincodeServiceExecuteV2EmbeddedTest.TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestRestTemplate
class ChaincodeServiceExecuteV2EmbeddedTest {

    class ScriptedChaincodeService(
        fabricGatewayClient: FabricGatewayClient,
        blockchainService: BlockchainServiceI,
        chaincodeConfiguration: C2ChaincodeConfiguration,
        cloudEventsProperties: CloudEventsProperties,
        private val scriptedOutcomes: ArrayDeque<TxOutcome>,
        private val raiseException: () -> Throwable? = { null },
    ) : ChaincodeService(fabricGatewayClient, blockchainService, chaincodeConfiguration, cloudEventsProperties) {

        override suspend fun runInvoke(envelope: InvokeEnvelope<InvokeRequest>): Pair<String, OutcomeData> {
            raiseException()?.let { throw it }
            val next = scriptedOutcomes.removeFirstOrNull()
                ?: error("no scripted TxOutcome available for envelope id=${envelope.id}")
            return next.toWire()
        }
    }

    @TestConfiguration
    class TestConfig {
        val scriptedOutcomes: ArrayDeque<TxOutcome> = ArrayDeque()
        var raiseException: () -> Throwable? = { null }

        @Bean
        @Primary
        fun chaincodeService(
            fabricGatewayClient: FabricGatewayClient,
            blockchainService: BlockchainServiceI,
            chaincodeConfiguration: C2ChaincodeConfiguration,
            cloudEventsProperties: CloudEventsProperties,
        ): ChaincodeService = ScriptedChaincodeService(
            fabricGatewayClient, blockchainService, chaincodeConfiguration, cloudEventsProperties,
            scriptedOutcomes, { raiseException() },
        )
    }

    @org.springframework.boot.test.web.server.LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var testConfig: TestConfig

    @Autowired
    lateinit var cloudEventsProperties: CloudEventsProperties

    private fun baseUrl() = UriComponentsBuilder.fromUriString("http://localhost:$port")

    private fun makeRequest(msgId: String) = InvokeEnvelope(
        id = msgId,
        type = InvokeType.Request.GENERIC,
        source = "/test-client",
        data = InvokeRequest(
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
        testConfig.raiseException = { null }
    }

    private fun post(vararg requests: InvokeEnvelope<InvokeRequest>): List<InvokeEnvelope<OutcomeData>> {
        val uri = baseUrl().path("invoke").build().toUri()
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(requests.toList(), headers)
        val typeRef = object : ParameterizedTypeReference<List<InvokeEnvelope<OutcomeData>>>() {}
        val response = restTemplate.exchange(uri, org.springframework.http.HttpMethod.POST, entity, typeRef)
        assertThat(response.statusCode.value()).isEqualTo(200)
        return response.body!!
    }

    private fun assertCommonCeAttrs(item: InvokeEnvelope<OutcomeData>, expectedSubject: String) {
        assertThat(item.specversion).isEqualTo("1.0")
        assertThat(item.source).isEqualTo(cloudEventsProperties.source)
        assertThat(item.subject).isEqualTo(expectedSubject)
        assertThat(item.id).isNotBlank()
        assertThat(item.time).isNotNull()
        assertThat(item.datacontenttype).isEqualTo("application/json")
    }

    @Test
    fun `Committed item carries committed type and transactionId blockNumber payload`() {
        testConfig.scriptedOutcomes += TxOutcome.Committed(
            msgId = "cmd-1", transactionId = "tx-abc", blockNumber = 42L, payload = "{}",
        )

        val outcomes = post(makeRequest("cmd-1"))

        assertThat(outcomes).hasSize(1)
        val item = outcomes[0]
        assertCommonCeAttrs(item, expectedSubject = "cmd-1")
        assertThat(item.type).isEqualTo(InvokeType.Outcome.COMMITTED)
        assertThat(item.data.transactionId).isEqualTo("tx-abc")
        assertThat(item.data.blockNumber).isEqualTo(42L)
        assertThat(item.data.payload).isEqualTo("{}")
        assertThat(item.data.errorCode).isNull()
        assertThat(item.data.errorMessage).isNull()
    }

    @Test
    fun `Rejected item carries rejected type and errorCode errorMessage`() {
        testConfig.scriptedOutcomes += TxOutcome.Rejected(
            msgId = "cmd-rej", errorCode = "ENDORSE_FAILED", errorMessage = "endorsement failed",
        )

        val outcomes = post(makeRequest("cmd-rej"))

        val item = outcomes.single()
        assertCommonCeAttrs(item, expectedSubject = "cmd-rej")
        assertThat(item.type).isEqualTo(InvokeType.Outcome.REJECTED)
        assertThat(item.data.errorCode).isEqualTo("ENDORSE_FAILED")
        assertThat(item.data.errorMessage).isEqualTo("endorsement failed")
        assertThat(item.data.transactionId).isNull()
        assertThat(item.data.blockNumber).isNull()
    }

    @Test
    fun `Transient item carries transient type and errorCode errorMessage`() {
        testConfig.scriptedOutcomes += TxOutcome.Transient(
            msgId = "cmd-tr", errorCode = "GRPC_UNAVAILABLE", errorMessage = "peer unreachable",
        )

        val item = post(makeRequest("cmd-tr")).single()
        assertCommonCeAttrs(item, expectedSubject = "cmd-tr")
        assertThat(item.type).isEqualTo(InvokeType.Outcome.TRANSIENT)
        assertThat(item.data.errorCode).isEqualTo("GRPC_UNAVAILABLE")
        assertThat(item.data.errorMessage).isEqualTo("peer unreachable")
    }

    @Test
    fun `Indeterminate item carries indeterminate type and errorCode errorMessage`() {
        testConfig.scriptedOutcomes += TxOutcome.Indeterminate(
            msgId = "cmd-ind", errorCode = "SUBMIT_FAILED", errorMessage = "orderer timeout",
        )

        val item = post(makeRequest("cmd-ind")).single()
        assertCommonCeAttrs(item, expectedSubject = "cmd-ind")
        assertThat(item.type).isEqualTo(InvokeType.Outcome.INDETERMINATE)
        assertThat(item.data.errorCode).isEqualTo("SUBMIT_FAILED")
        assertThat(item.data.errorMessage).isEqualTo("orderer timeout")
    }

    @Test
    fun `Conflict item carries conflict type with transactionId blockNumber AND errorCode errorMessage`() {
        testConfig.scriptedOutcomes += TxOutcome.Conflict(
            msgId = "cmd-cf",
            errorCode = "MVCC_READ_CONFLICT",
            errorMessage = "conflict on key session-xyz",
            transactionId = "tx-conflict",
            blockNumber = 7L,
        )

        val item = post(makeRequest("cmd-cf")).single()
        assertCommonCeAttrs(item, expectedSubject = "cmd-cf")
        assertThat(item.type).isEqualTo(InvokeType.Outcome.CONFLICT)
        assertThat(item.data.errorCode).isEqualTo("MVCC_READ_CONFLICT")
        assertThat(item.data.errorMessage).isEqualTo("conflict on key session-xyz")
        assertThat(item.data.transactionId).isEqualTo("tx-conflict")
        assertThat(item.data.blockNumber).isEqualTo(7L)
    }

    @Test
    fun `gateway exception is wrapped as Rejected with GATEWAY_EXCEPTION code`() {
        testConfig.raiseException = { IllegalStateException("simulated gateway failure") }

        val item = post(makeRequest("cmd-exc")).single()
        assertCommonCeAttrs(item, expectedSubject = "cmd-exc")
        assertThat(item.type).isEqualTo(InvokeType.Outcome.REJECTED)
        assertThat(item.data.errorCode).isEqualTo("GATEWAY_EXCEPTION")
        assertThat(item.data.errorMessage).isEqualTo("simulated gateway failure")
    }
}
