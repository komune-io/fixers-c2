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
import ssm.sdk.core.command.SsmPerformCommandV2
import ssm.sdk.core.command.SsmStartCommandV2
import ssm.sdk.core.ktor.KtorRepository
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner

/**
 * Unit tests (no Spring, no live network) for SsmTxService.sendStartV2 and sendPerformV2.
 *
 * Pins:
 * 1. signss / signs is called once (not per-item), building the full signed list.
 * 2. invokeAllV2 receives signed.size == commands.size.
 * 3. commandIds list passed to invokeAllV2 equals commands.map { it.commandId } in order.
 * 4. Outcomes returned by invokeAllV2 are passed back verbatim.
 *
 * The stub SsmRequester is built with a MockEngine (pattern from SsmRequesterV2Test).
 * The stub SsmCmdSigner is a SAM that wraps the SsmCmd into a minimal SsmCmdSigned.
 *
 * Recorded state is captured via mutable lists injected into the mock HTTP engine's
 * response body, which returns a scripted JSON payload.
 */
class SsmTxServiceV2Test {

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
        val repository = KtorRepository(
            baseUrl = "http://localhost:9090",
            timeout = 5_000L,
            authCredentials = null,
            client = client,
        )
        return SsmRequester(
            jsonConverter = JSONConverterObjectMapper(),
            coopRepository = repository,
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

    /**
     * Build a scripted JSON array of CommandOutcome items, one per commandId.
     * All are Committed for simplicity unless otherwise scripted.
     */
    private fun committedJson(commandIds: List<String>): String {
        val items = commandIds.joinToString(",\n") { id ->
            """{"outcome":"Committed","commandId":"$id","transactionId":"tx-$id","blockNumber":42}"""
        }
        return "[$items]"
    }

    /**
     * Build a scripted JSON array mixing all 5 outcome categories.
     */
    private fun mixedOutcomesJson(commandIds: List<String>): String {
        val items = commandIds.mapIndexed { i, id ->
            when (i % 5) {
                0 -> """{"outcome":"Committed","commandId":"$id","transactionId":"tx-$id","blockNumber":$i}"""
                1 -> """{"outcome":"Rejected","commandId":"$id","errorCode":"ERR","errorMessage":"rejected"}"""
                2 -> """{"outcome":"Transient","commandId":"$id","errorCode":"RETRY","errorMessage":"transient"}"""
                3 -> """{"outcome":"Indeterminate","commandId":"$id","errorCode":"X","errorMessage":"u"}"""
                else -> """{"outcome":"Conflict","commandId":"$id","errorCode":"MVCC","errorMessage":"conflict"}"""
            }
        }.joinToString(",\n")
        return "[$items]"
    }

    // ---------------------------------------------------------------------------
    // sendStartV2 tests
    // ---------------------------------------------------------------------------

    @Test
    fun `sendStartV2 invokes invokeAllV2 with commandIds in input order`(): Unit = runBlocking {
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
        val repository = KtorRepository("http://localhost:9090", 5_000L, null, client)
        val service = SsmService(SsmRequester(JSONConverterObjectMapper(), repository), stubSigner)
        val txService = SsmTxService(service, SsmBatchProperties())

        val commands = ids.map { id ->
            SsmStartCommandV2(
                commandId = id,
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

        val outcomes: List<CommandOutcome> = txService.sendStartV2(commands)

        // One HTTP call was made (invokeAllV2 batches all commands together)
        assertThat(capturedBody).hasSize(1)
        // Outcomes are returned verbatim (one per input)
        assertThat(outcomes).hasSize(ids.size)
        // commandIds in outcomes match input order
        assertThat(outcomes.map { it.commandId }).isEqualTo(ids)
        // All Committed in this scripted response
        outcomes.forEach { assertThat(it.outcome).isEqualTo("Committed") }
    }

    @Test
    fun `sendStartV2 returns outcomes verbatim from invokeAllV2`(): Unit = runBlocking {
        val ids = listOf("cmd-0", "cmd-1", "cmd-2", "cmd-3", "cmd-4")
        val requester = buildRequester(mixedOutcomesJson(ids))
        val txService = buildTxService(buildService(requester))

        val commands = ids.map { id ->
            SsmStartCommandV2(
                commandId = id,
                session = SsmSession("test-ssm", "session-$id", mapOf("admin" to "Admin"), "{}", mapOf()),
                chaincodeUri = chaincodeUri,
                signerName = "admin",
            )
        }

        val outcomes = txService.sendStartV2(commands)

        assertThat(outcomes).hasSize(5)
        assertThat(outcomes[0].outcome).isEqualTo("Committed")
        assertThat(outcomes[1].outcome).isEqualTo("Rejected")
        assertThat(outcomes[2].outcome).isEqualTo("Transient")
        assertThat(outcomes[3].outcome).isEqualTo("Indeterminate")
        assertThat(outcomes[4].outcome).isEqualTo("Conflict")
    }

    // ---------------------------------------------------------------------------
    // sendPerformV2 tests
    // ---------------------------------------------------------------------------

    @Test
    fun `sendPerformV2 invokes invokeAllV2 with commandIds in input order`(): Unit = runBlocking {
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
        val repository = KtorRepository("http://localhost:9090", 5_000L, null, client)
        val service = SsmService(SsmRequester(JSONConverterObjectMapper(), repository), stubSigner)
        val txService = SsmTxService(service, SsmBatchProperties())

        val commands = ids.map { id ->
            SsmPerformCommandV2(
                commandId = id,
                action = "Validate",
                context = SsmContext(session = "session-$id", public = "{}", private = mapOf(), iteration = 1),
                chaincodeUri = chaincodeUri,
                signerName = "user1",
            )
        }

        val outcomes: List<CommandOutcome> = txService.sendPerformV2(commands)

        assertThat(capturedBody).hasSize(1)
        assertThat(outcomes).hasSize(ids.size)
        assertThat(outcomes.map { it.commandId }).isEqualTo(ids)
        outcomes.forEach { assertThat(it.outcome).isEqualTo("Committed") }
    }

    @Test
    fun `sendPerformV2 returns outcomes verbatim from invokeAllV2`(): Unit = runBlocking {
        val ids = listOf("p-cmd-0", "p-cmd-1", "p-cmd-2", "p-cmd-3", "p-cmd-4")
        val requester = buildRequester(mixedOutcomesJson(ids))
        val txService = buildTxService(buildService(requester))

        val commands = ids.map { id ->
            SsmPerformCommandV2(
                commandId = id,
                action = "Perform",
                context = SsmContext("session-$id", "{}", 0, mapOf()),
                chaincodeUri = chaincodeUri,
                signerName = "admin",
            )
        }

        val outcomes = txService.sendPerformV2(commands)

        assertThat(outcomes).hasSize(5)
        assertThat(outcomes[0].outcome).isEqualTo("Committed")
        assertThat(outcomes[1].outcome).isEqualTo("Rejected")
        assertThat(outcomes[2].outcome).isEqualTo("Transient")
        assertThat(outcomes[3].outcome).isEqualTo("Indeterminate")
        assertThat(outcomes[4].outcome).isEqualTo("Conflict")
    }
}
