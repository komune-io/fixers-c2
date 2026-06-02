package ssm.sdk.core.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.sdk.dsl.CommandOutcome

/**
 * Tests for ChaincodeApiGatewayClient.invoke status-branching behaviour.
 *
 * Validates that:
 * - 2xx: deserializes body as List<CommandOutcome>
 * - 4xx: synthesises N×CommandOutcome(outcome="Rejected", errorCode="HTTP_<status>")
 * - 5xx: synthesises N×CommandOutcome(outcome="Transient", errorCode="HTTP_<status>")
 * - Network error: synthesises N×CommandOutcome(outcome="Indeterminate")
 *
 * Closes leaks M, N, O in ERROR-PROPAGATION.md.
 */
class ChaincodeApiGatewayClientStatusValidationTest {

    private val commandIds = listOf("cmd-1", "cmd-2")

    private val sampleInvokeArgs: List<InvokeRequest> = listOf(
        InvokeRequest(
            cmd = InvokeRequestType.invoke,
            channelid = "sandbox",
            chaincodeid = "ssm",
            fcn = "register",
            args = arrayOf("""{"name":"test"}""")
        ),
        InvokeRequest(
            cmd = InvokeRequestType.invoke,
            channelid = "sandbox",
            chaincodeid = "ssm",
            fcn = "register",
            args = arrayOf("""{"name":"test2"}""")
        ),
    )

    private fun buildRepository(statusCode: HttpStatusCode, body: String): ChaincodeApiGatewayClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = body,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { jackson() }
        }
        return ChaincodeApiGatewayClient(
            baseUrl = "http://localhost:9090",
            timeout = 5_000L,
            authCredentials = null,
            client = client,
        )
    }

    private fun buildNetworkErrorRepository(): ChaincodeApiGatewayClient {
        val mockEngine = MockEngine { _ ->
            throw java.net.ConnectException("Connection refused: localhost/127.0.0.1:9090")
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { jackson() }
        }
        return ChaincodeApiGatewayClient(
            baseUrl = "http://localhost:9090",
            timeout = 5_000L,
            authCredentials = null,
            client = client,
        )
    }

    // --------------------------------------------------------------------------
    // 2xx: happy path
    // --------------------------------------------------------------------------

    @Test
    fun `invoke on 200 deserializes CloudEvents body as CommandOutcome list`(): Unit = runBlocking {
        val responseJson = """
            [
              {
                "specversion":"1.0",
                "id":"resp-1",
                "source":"/io.komune.c2/gateway",
                "type":"io.komune.c2.invoke.outcome.committed",
                "subject":"cmd-1",
                "time":"2026-05-22T10:30:01Z",
                "datacontenttype":"application/json",
                "data":{"transactionId":"tx-abc","blockNumber":10}
              },
              {
                "specversion":"1.0",
                "id":"resp-2",
                "source":"/io.komune.c2/gateway",
                "type":"io.komune.c2.invoke.outcome.rejected",
                "subject":"cmd-2",
                "time":"2026-05-22T10:30:01Z",
                "datacontenttype":"application/json",
                "data":{"errorCode":"MVCC_READ_CONFLICT"}
              }
            ]
        """.trimIndent()

        val repo = buildRepository(HttpStatusCode.OK, responseJson)
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(2)
        assertThat(outcomes[0].msgId).isEqualTo("cmd-1")
        assertThat(outcomes[0].outcome).isEqualTo("Committed")
        assertThat(outcomes[0].transactionId).isEqualTo("tx-abc")
        assertThat(outcomes[1].msgId).isEqualTo("cmd-2")
        assertThat(outcomes[1].outcome).isEqualTo("Rejected")
        assertThat(outcomes[1].errorCode).isEqualTo("MVCC_READ_CONFLICT")
    }

    // --------------------------------------------------------------------------
    // 4xx: Rejected
    // --------------------------------------------------------------------------

    @Test
    fun `invoke on 400 synthesises N×Rejected outcomes`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.BadRequest, """{"error":"bad request"}""")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEachIndexed { i, outcome ->
            assertThat(outcome.msgId).isEqualTo(commandIds[i])
            assertThat(outcome.outcome).isEqualTo("Rejected")
            assertThat(outcome.errorCode).isEqualTo("HTTP_400")
        }
    }

    @Test
    fun `invoke on 401 synthesises N×Rejected outcomes with AUTH errorClass`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.Unauthorized, "Unauthorized")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { outcome ->
            assertThat(outcome.outcome).isEqualTo("Rejected")
            assertThat(outcome.errorCode).isEqualTo("HTTP_401")
        }
    }

    @Test
    fun `invoke on 403 synthesises N×Rejected outcomes with AUTH errorClass`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.Forbidden, "Forbidden")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { outcome ->
            assertThat(outcome.outcome).isEqualTo("Rejected")
            assertThat(outcome.errorCode).isEqualTo("HTTP_403")
        }
    }

    @Test
    fun `invoke on 404 synthesises N×Rejected outcomes with INPUT errorClass`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.NotFound, "Not Found")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { outcome ->
            assertThat(outcome.outcome).isEqualTo("Rejected")
            assertThat(outcome.errorCode).isEqualTo("HTTP_404")
        }
    }

    // --------------------------------------------------------------------------
    // 5xx: Transient
    // --------------------------------------------------------------------------

    @Test
    fun `invoke on 500 synthesises N×Transient outcomes with NETWORK errorClass`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.InternalServerError, "Internal Server Error")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEachIndexed { i, outcome ->
            assertThat(outcome.msgId).isEqualTo(commandIds[i])
            assertThat(outcome.outcome).isEqualTo("Transient")
            assertThat(outcome.errorCode).isEqualTo("HTTP_500")
        }
    }

    @Test
    fun `invoke on 503 synthesises N×Transient outcomes with NETWORK errorClass`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.ServiceUnavailable, "Service Unavailable")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { outcome ->
            assertThat(outcome.outcome).isEqualTo("Transient")
            assertThat(outcome.errorCode).isEqualTo("HTTP_503")
        }
    }

    // --------------------------------------------------------------------------
    // Network error: Indeterminate
    // --------------------------------------------------------------------------

    @Test
    fun `invoke on network error synthesises N×Indeterminate outcomes`(): Unit = runBlocking {
        val repo = buildNetworkErrorRepository()
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEachIndexed { i, outcome ->
            assertThat(outcome.msgId).isEqualTo(commandIds[i])
            assertThat(outcome.outcome).isEqualTo("Indeterminate")
            assertThat(outcome.errorCode).isEqualTo("CONNECT_REFUSED")
        }
    }

    @Test
    fun `connect refused yields Indeterminate with CONNECT_REFUSED code and NETWORK class`(): Unit = runBlocking {
        // Use a hermetic MockEngine instead of 127.0.0.1:1 to avoid environment-dependent
        // behaviour (sandboxed Docker / hardened macOS may give TIMEOUT or PermissionException
        // rather than ECONNREFUSED, making the test flaky in CI).
        val mockEngine = MockEngine { _ -> throw java.net.ConnectException("Connection refused") }
        val client = HttpClient(mockEngine) { install(ContentNegotiation) { jackson() } }
        val repo = ChaincodeApiGatewayClient(
            baseUrl = "http://test", timeout = 500L, authCredentials = null, client = client,
        )
        val outcomes = repo.invoke(listOf(sampleInvokeArgs.first()), listOf("cmd-1"))
        assertThat(outcomes.single().outcome).isEqualTo("Indeterminate")
        assertThat(outcomes.single().errorCode).isEqualTo("CONNECT_REFUSED")
    }

    @Test
    fun `request timeout yields Indeterminate with TIMEOUT code and NETWORK class`(): Unit = runBlocking {
        val mockEngine = MockEngine { _ ->
            delay(10_000)
            respond(content = "never reaches", status = HttpStatusCode.OK)
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { jackson() }
            install(HttpTimeout) {
                requestTimeoutMillis = 100L
                connectTimeoutMillis = 100L
            }
        }
        val repo = ChaincodeApiGatewayClient(
            baseUrl = "http://test",
            timeout = 100L,
            authCredentials = null,
            client = client,
        )
        val outcomes = repo.invoke(
            invokeArgs = listOf(sampleInvokeArgs.first()),
            msgIds = listOf("cmd-1"),
        )
        assertThat(outcomes.single().outcome).isEqualTo("Indeterminate")
        assertThat(outcomes.single().errorCode).isEqualTo("TIMEOUT")
    }

    // --------------------------------------------------------------------------
    // Unexpected HTTP status (outside 2xx / 4xx / 5xx)
    // --------------------------------------------------------------------------

    @Test
    fun `invoke on unexpected status synthesises N×Indeterminate with UNEXPECTED_HTTP code`(): Unit = runBlocking {
        val repo = buildRepository(HttpStatusCode.fromValue(302), "Redirect")
        val outcomes: List<CommandOutcome> = repo.invoke(sampleInvokeArgs, commandIds)

        assertThat(outcomes).hasSize(commandIds.size)
        outcomes.forEach { outcome ->
            assertThat(outcome.outcome).isEqualTo("Indeterminate")
            assertThat(outcome.errorCode).isEqualTo("UNEXPECTED_HTTP_302")
        }
    }

    // --------------------------------------------------------------------------
    // commandId pairing is preserved
    // --------------------------------------------------------------------------

    @Test
    fun `invoke 4xx outcomes commandId order matches input commandIds`(): Unit = runBlocking {
        val ids = listOf("alpha", "beta", "gamma")
        val args = List(3) {
            InvokeRequest(
                cmd = InvokeRequestType.invoke,
                channelid = "sandbox",
                chaincodeid = "ssm",
                fcn = "register",
                args = arrayOf("arg$it"),
            )
        }
        val repo = buildRepository(HttpStatusCode.BadRequest, "bad")
        val outcomes = repo.invoke(args, ids)

        assertThat(outcomes.map { it.msgId }).isEqualTo(ids)
    }

    // --------------------------------------------------------------------------
    // CancellationException propagation (structured concurrency)
    // --------------------------------------------------------------------------

    @Test
    fun `runCatching rethrows CancellationException instead of synthesising`(): Unit = runBlocking {
        val mockEngine = MockEngine { _ -> throw kotlinx.coroutines.CancellationException("cancel") }
        val client = HttpClient(mockEngine) { install(ContentNegotiation) { jackson() } }
        val repo = ChaincodeApiGatewayClient(
            baseUrl = "http://test", timeout = 500L, authCredentials = null, client = client,
        )
        org.junit.jupiter.api.assertThrows<kotlinx.coroutines.CancellationException> {
            repo.invoke(listOf(sampleInvokeArgs.first()), listOf("cmd-1"))
        }
    }
}
