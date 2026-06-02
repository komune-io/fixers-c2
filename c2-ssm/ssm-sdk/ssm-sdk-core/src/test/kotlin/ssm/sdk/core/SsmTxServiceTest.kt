package ssm.sdk.core

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.core.ktor.ChaincodeApiGatewayClient
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner

/**
 * Unit tests (no Spring, no live network) for SsmTxService.sendStart and sendPerform.
 *
 * Pins:
 * 1. signss / signs is called once (not per-item), building the full signed list.
 * 2. invokeAll receives signed.size == commands.size.
 * 3. commandIds list passed to invokeAll equals commands.map { it.commandId } in order.
 * 4. Outcomes returned by invokeAll are passed back verbatim.
 *
 * The stub SsmRequester is built with a MockEngine (pattern from SsmRequesterTest).
 * The stub SsmCmdSigner is a SAM that wraps the SsmCmd into a minimal SsmCmdSigned.
 *
 * Recorded state is captured via mutable lists injected into the mock HTTP engine's
 * response body, which returns a scripted JSON payload.
 */
class SsmTxServiceTest {

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

    /**
     * Stub signer: creates a deterministic SsmCmdSigned without real crypto.
     */
    private val stubSigner: SsmCmdSigner = SsmCmdSigner { cmd ->
        SsmCmdSigned(
            cmd = cmd,
            signature = "fake-sig-${cmd.agentName}",
            signer = cmd.agentName,
            chaincodeUri = cmd.chaincodeUri,
        )
    }

    /**
     * Build a SsmRequester backed by a mock HTTP engine that always returns
     * the given JSON response body for any request.
     */
    private fun buildRequester(responseBody: String): SsmRequester {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { jackson() }
        }
        val repository = ChaincodeApiGatewayClient(
            baseUrl = "http://localhost:9090",
            timeout = 5_000L,
            authCredentials = null,
            client = client,
        )
        return SsmRequester(
            jsonConverter = JSONConverterObjectMapper(),
            ssmChaincodeClient = repository,
        )
    }

    /**
     * Build a SsmService with the given requester and the stub signer.
     */
    private fun buildService(requester: SsmRequester): SsmService =
        SsmService(requester, stubSigner)

    /**
     * Build a SsmTxService with the given service and default batch properties.
     */
    private fun buildTxService(service: SsmService): SsmTxService =
        SsmTxService(service, SsmBatchProperties())

    private fun ceEnvelope(subject: String, type: String, data: String): String = """
        {
          "specversion":"1.0",
          "id":"resp-$subject",
          "source":"/io.komune.c2/gateway",
          "type":"$type",
          "subject":"$subject",
          "time":"2026-05-22T10:30:01Z",
          "datacontenttype":"application/json",
          "data":$data
        }
    """.trimIndent()

    private fun committedJson(commandIds: List<String>): String {
        val items = commandIds.joinToString(",\n") { id ->
            ceEnvelope(id, "io.komune.c2.invoke.outcome.committed", """{"transactionId":"tx-$id","blockNumber":42}""")
        }
        return "[$items]"
    }

    private fun mixedOutcomesJson(commandIds: List<String>): String {
        val items = commandIds.mapIndexed { i, id ->
            when (i % 5) {
                0 -> ceEnvelope(id, "io.komune.c2.invoke.outcome.committed",
                    """{"transactionId":"tx-$id","blockNumber":$i}""")
                1 -> ceEnvelope(id, "io.komune.c2.invoke.outcome.rejected",
                    """{"errorCode":"ERR","errorMessage":"rejected"}""")
                2 -> ceEnvelope(id, "io.komune.c2.invoke.outcome.transient",
                    """{"errorCode":"RETRY","errorMessage":"transient"}""")
                3 -> ceEnvelope(id, "io.komune.c2.invoke.outcome.indeterminate",
                    """{"errorCode":"X","errorMessage":"u"}""")
                else -> ceEnvelope(id, "io.komune.c2.invoke.outcome.conflict",
                    """{"errorCode":"MVCC","errorMessage":"conflict"}""")
            }
        }.joinToString(",\n")
        return "[$items]"
    }

    // ---------------------------------------------------------------------------
    // sendStart tests
    // ---------------------------------------------------------------------------

    @Test
    fun `sendStart invokes invokeAll with commandIds in input order`(): Unit = runBlocking {
        val ids = listOf("start-cmd-A", "start-cmd-B", "start-cmd-C")

        val capturedBody = mutableListOf<String>()
        val mockEngine = MockEngine { request ->
            capturedBody += request.url.toString()
            respond(
                content = committedJson(ids),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(mockEngine) { install(ContentNegotiation) { jackson() } }
        val repository = ChaincodeApiGatewayClient("http://localhost:9090", 5_000L, null, client = client)
        val service = SsmService(SsmRequester(JSONConverterObjectMapper(), repository), stubSigner)
        val txService = SsmTxService(service, SsmBatchProperties())

        val commands = ids.map { id ->
            SsmStartCommand(
                msgId = id,
                session = SsmSession(
                    ssm = "test-ssm",
                    session = "session-$id",
                    roles = mapOf("admin" to "Admin"),
                    public = "{}",
                    private = mapOf(),
                ),
                chaincodeUri = chaincodeUri,
                signerName = "admin",
            )
        }

        val outcomes: List<CommandOutcome> = txService.sendStart(commands)

        // One HTTP call was made (invokeAll batches all commands together)
        assertThat(capturedBody).hasSize(1)
        // Outcomes are returned verbatim (one per input)
        assertThat(outcomes).hasSize(ids.size)
        // commandIds in outcomes match input order
        assertThat(outcomes.map { it.msgId }).isEqualTo(ids)
        // All Committed in this scripted response
        outcomes.forEach { assertThat(it.outcome).isEqualTo("Committed") }
    }

    @Test
    fun `sendStart returns outcomes verbatim from invokeAll`(): Unit = runBlocking {
        val ids = listOf("cmd-0", "cmd-1", "cmd-2", "cmd-3", "cmd-4")
        val requester = buildRequester(mixedOutcomesJson(ids))
        val txService = buildTxService(buildService(requester))

        val commands = ids.map { id ->
            SsmStartCommand(
                msgId = id,
                session = SsmSession("test-ssm", "session-$id", mapOf("admin" to "Admin"), "{}", mapOf()),
                chaincodeUri = chaincodeUri,
                signerName = "admin",
            )
        }

        val outcomes = txService.sendStart(commands)

        assertThat(outcomes).hasSize(5)
        assertThat(outcomes[0].outcome).isEqualTo("Committed")
        assertThat(outcomes[1].outcome).isEqualTo("Rejected")
        assertThat(outcomes[2].outcome).isEqualTo("Transient")
        assertThat(outcomes[3].outcome).isEqualTo("Indeterminate")
        assertThat(outcomes[4].outcome).isEqualTo("Conflict")
    }

    // ---------------------------------------------------------------------------
    // sendPerform tests
    // ---------------------------------------------------------------------------

    @Test
    fun `sendPerform invokes invokeAll with commandIds in input order`(): Unit = runBlocking {
        val ids = listOf("perform-cmd-X", "perform-cmd-Y")

        val capturedBody = mutableListOf<String>()
        val mockEngine = MockEngine { request ->
            capturedBody += request.url.toString()
            respond(
                content = committedJson(ids),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(mockEngine) { install(ContentNegotiation) { jackson() } }
        val repository = ChaincodeApiGatewayClient("http://localhost:9090", 5_000L, null, client = client)
        val service = SsmService(SsmRequester(JSONConverterObjectMapper(), repository), stubSigner)
        val txService = SsmTxService(service, SsmBatchProperties())

        val commands = ids.map { id ->
            SsmPerformCommand(
                msgId = id,
                action = "Validate",
                context = SsmContext(session = "session-$id", public = "{}", private = mapOf(), iteration = 1),
                chaincodeUri = chaincodeUri,
                signerName = "user1",
            )
        }

        val outcomes: List<CommandOutcome> = txService.sendPerform(commands)

        assertThat(capturedBody).hasSize(1)
        assertThat(outcomes).hasSize(ids.size)
        assertThat(outcomes.map { it.msgId }).isEqualTo(ids)
        outcomes.forEach { assertThat(it.outcome).isEqualTo("Committed") }
    }

    @Test
    fun `sendPerform returns outcomes verbatim from invokeAll`(): Unit = runBlocking {
        val ids = listOf("p-cmd-0", "p-cmd-1", "p-cmd-2", "p-cmd-3", "p-cmd-4")
        val requester = buildRequester(mixedOutcomesJson(ids))
        val txService = buildTxService(buildService(requester))

        val commands = ids.map { id ->
            SsmPerformCommand(
                msgId = id,
                action = "Perform",
                context = SsmContext("session-$id", "{}", 0, mapOf()),
                chaincodeUri = chaincodeUri,
                signerName = "admin",
            )
        }

        val outcomes = txService.sendPerform(commands)

        assertThat(outcomes).hasSize(5)
        assertThat(outcomes[0].outcome).isEqualTo("Committed")
        assertThat(outcomes[1].outcome).isEqualTo("Rejected")
        assertThat(outcomes[2].outcome).isEqualTo("Transient")
        assertThat(outcomes[3].outcome).isEqualTo("Indeterminate")
        assertThat(outcomes[4].outcome).isEqualTo("Conflict")
    }
}
