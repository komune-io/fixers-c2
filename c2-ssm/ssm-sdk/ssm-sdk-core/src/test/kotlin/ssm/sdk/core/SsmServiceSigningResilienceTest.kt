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
import ssm.sdk.core.ktor.KtorRepository
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner

/**
 * Tests for SsmService.invokeAll per-item signing resilience.
 *
 * Validates that:
 * - A signing failure for one item produces CommandOutcome(outcome="Rejected", errorCode="SIGN_FAILED")
 *   for that item without aborting the whole batch.
 * - Successfully signed items are still sent and their outcomes returned.
 * - All commandIds are covered in the returned list (sign-failure + invoke outcomes).
 *
 * Closes leak O in ERROR-PROPAGATION.md.
 */
class SsmServiceSigningResilienceTest {

    private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

    /** Stub signer that succeeds for all commands. */
    private val alwaysSucceedSigner: SsmCmdSigner = SsmCmdSigner { cmd ->
        SsmCmdSigned(
            cmd = cmd,
            signature = "fake-sig",
            signer = cmd.agentName,
            chaincodeUri = cmd.chaincodeUri,
        )
    }

    /**
     * Partial-failure signer: throws for commandIds containing "bad",
     * succeeds for everything else.
     */
    private fun partialFailSigner(failAgentName: String): SsmCmdSigner = SsmCmdSigner { cmd ->
        if (cmd.agentName == failAgentName) {
            throw IllegalStateException("Key not found for agent: ${cmd.agentName}")
        }
        SsmCmdSigned(
            cmd = cmd,
            signature = "fake-sig",
            signer = cmd.agentName,
            chaincodeUri = cmd.chaincodeUri,
        )
    }

    private fun buildCmd(agentName: String): SsmCmd = SsmCmd(
        chaincodeUri = chaincodeUri,
        agentName = agentName,
        json = """{"name":"test"}""",
        command = SsmCmdName.REGISTER,
        performAction = null,
        valueToSign = """{"name":"test"}""",
    )

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
            ssmRequesterRepository = repository,
        )
    }

    private fun committedJson(commandIds: List<String>): String {
        val items = commandIds.joinToString(",\n") { id ->
            """
                {
                  "specversion":"1.0",
                  "id":"resp-$id",
                  "source":"/io.komune.c2/gateway",
                  "type":"io.komune.c2.invoke.outcome.committed",
                  "subject":"$id",
                  "time":"2026-05-22T10:30:01Z",
                  "datacontenttype":"application/json",
                  "data":{"transactionId":"tx-$id","blockNumber":1}
                }
            """.trimIndent()
        }
        return "[$items]"
    }

    // --------------------------------------------------------------------------
    // All signs succeed: normal path
    // --------------------------------------------------------------------------

    @Test
    fun `invokeAll all-success signing path returns all outcomes`(): Unit = runBlocking {
        val commandIds = listOf("cmd-A", "cmd-B", "cmd-C")
        val cmds = commandIds.map { buildCmd("admin") }
        val requester = buildRequester(committedJson(commandIds))
        val service = SsmService(requester, alwaysSucceedSigner)

        val outcomes: List<CommandOutcome> = service.invokeAll(
            cmds = cmds,
            msgIds = commandIds,
        )

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { assertThat(it.outcome).isEqualTo("Committed") }
        assertThat(outcomes.map { it.msgId }).isEqualTo(commandIds)
    }

    // --------------------------------------------------------------------------
    // One sign fails: that item is Rejected, rest succeed
    // --------------------------------------------------------------------------

    @Test
    fun `invokeAll single signing failure produces Rejected outcome for that item`(): Unit = runBlocking {
        val commandIds = listOf("cmd-good-1", "cmd-good-2")
        // Build cmds: first uses "admin" (good), second uses "bad-agent" (fail)
        val cmds = listOf(
            buildCmd("admin"),
            buildCmd("bad-agent"),
        )

        // Mock returns Committed only for the successfully-signed item
        val successIds = listOf("cmd-good-1")
        val requester = buildRequester(committedJson(successIds))
        val service = SsmService(requester, partialFailSigner("bad-agent"))

        val outcomes: List<CommandOutcome> = service.invokeAll(
            cmds = cmds,
            msgIds = commandIds,
        )

        assertThat(outcomes).hasSize(commandIds.size)

        val good = outcomes.first { it.msgId == "cmd-good-1" }
        assertThat(good.outcome).isEqualTo("Committed")

        val failed = outcomes.first { it.msgId == "cmd-good-2" }
        assertThat(failed.outcome).isEqualTo("Rejected")
        assertThat(failed.errorCode).isEqualTo("SIGN_FAILED")
        assertThat(failed.errorMessage).contains("Key not found for agent: bad-agent")
    }

    @Test
    fun `invokeAll signing failure outcome has correct commandId`(): Unit = runBlocking {
        val commandIds = listOf("first-cmd", "second-cmd")
        val cmds = listOf(
            buildCmd("bad-agent"), // first-cmd — signing fails
            buildCmd("admin"),     // second-cmd — signing succeeds
        )

        val successIds = listOf("second-cmd")
        val requester = buildRequester(committedJson(successIds))
        val service = SsmService(requester, partialFailSigner("bad-agent"))

        val outcomes: List<CommandOutcome> = service.invokeAll(
            cmds = cmds,
            msgIds = commandIds,
        )

        assertThat(outcomes).hasSize(2)
        val failedOutcome = outcomes.first { it.outcome == "Rejected" }
        assertThat(failedOutcome.msgId).isEqualTo("first-cmd")
        assertThat(failedOutcome.errorCode).isEqualTo("SIGN_FAILED")
    }

    // --------------------------------------------------------------------------
    // All signs fail: all outcomes are Rejected, no HTTP call needed
    // --------------------------------------------------------------------------

    @Test
    fun `invokeAll all-signing-failure produces Rejected for all items`(): Unit = runBlocking {
        val commandIds = listOf("cmd-X", "cmd-Y", "cmd-Z")
        val cmds = commandIds.map { buildCmd("bad-agent") }

        val requester = buildRequester("[]")
        val service = SsmService(requester, partialFailSigner("bad-agent"))

        val outcomes: List<CommandOutcome> = service.invokeAll(
            cmds = cmds,
            msgIds = commandIds,
        )

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { outcome ->
            assertThat(outcome.outcome).isEqualTo("Rejected")
            assertThat(outcome.errorCode).isEqualTo("SIGN_FAILED")
        }
        assertThat(outcomes.map { it.msgId }).containsExactlyInAnyOrderElementsOf(commandIds)
    }

    // --------------------------------------------------------------------------
    // Preserved commandId in mixed results
    // --------------------------------------------------------------------------

    @Test
    fun `invokeAll mixed results with all commandIds present in output`(): Unit = runBlocking {
        val commandIds = listOf("good-1", "bad-1", "good-2")
        val cmds = listOf(
            buildCmd("admin"),
            buildCmd("bad-agent"),
            buildCmd("admin"),
        )

        val successIds = listOf("good-1", "good-2")
        val requester = buildRequester(committedJson(successIds))
        val service = SsmService(requester, partialFailSigner("bad-agent"))

        val outcomes: List<CommandOutcome> = service.invokeAll(
            cmds = cmds,
            msgIds = commandIds,
        )

        assertThat(outcomes).hasSize(3)
        assertThat(outcomes.map { it.msgId })
            .containsExactlyInAnyOrderElementsOf(commandIds)

        val bad = outcomes.first { it.msgId == "bad-1" }
        assertThat(bad.outcome).isEqualTo("Rejected")
        assertThat(bad.errorCode).isEqualTo("SIGN_FAILED")
    }
}
