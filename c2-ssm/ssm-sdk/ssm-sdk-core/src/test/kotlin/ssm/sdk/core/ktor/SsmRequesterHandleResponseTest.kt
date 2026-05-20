package ssm.sdk.core.ktor

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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import ssm.sdk.dsl.InvokeException
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.json.JSONConverterObjectMapper

/**
 * Tests for SsmRequester.handleResponse — verifying that transport errors
 * are preserved in the InvokeException message (no SwallowedException).
 *
 * Closes Sub-fix A in ERROR-PROPAGATION-PLAN.md.
 */
class SsmRequesterHandleResponseTest {

    /**
     * Build a SsmRequester backed by a mock HTTP engine that always returns
     * the given [body] with the given [status].
     */
    private fun buildRequesterWithBody(body: String, status: HttpStatusCode = HttpStatusCode.OK): SsmRequester {
        val mockEngine = MockEngine { _ ->
            respond(
                content = body,
                status = status,
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

    private fun dummySignedCmd(chaincodeUri: ChaincodeUri): SsmCmdSigned {
        val cmd = SsmCmd(
            chaincodeUri = chaincodeUri,
            agentName = "admin",
            json = """{"name":"test"}""",
            command = SsmCmdName.REGISTER,
            performAction = null,
            valueToSign = """{"name":"test"}""",
        )
        return SsmCmdSigned(
            cmd = cmd,
            signature = "fakesig",
            signer = "admin",
            chaincodeUri = chaincodeUri,
        )
    }

    // --------------------------------------------------------------------------
    // Non-JSON body → message must include the body excerpt, NOT swallow it
    // --------------------------------------------------------------------------

    @Test
    fun `handleResponse with non-JSON body includes body excerpt in InvokeException message`(): Unit = runBlocking {
        val nonJsonBody = "Gateway error: chaincode registration failed"
        val requester = buildRequesterWithBody(nonJsonBody)
        val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

        assertThatThrownBy {
            runBlocking { requester.invoke(dummySignedCmd(chaincodeUri)) }
        }
            .isInstanceOf(InvokeException::class.java)
            .hasMessageContaining(nonJsonBody)
    }

    @Test
    fun `handleResponse with empty body includes empty-marker in InvokeException message`(): Unit = runBlocking {
        val requester = buildRequesterWithBody("")
        val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

        assertThatThrownBy {
            runBlocking { requester.invoke(dummySignedCmd(chaincodeUri)) }
        }
            .isInstanceOf(InvokeException::class.java)
            .hasMessageContaining("<empty>")
    }

    @Test
    fun `handleResponse with old InvokeError JSON body includes the JSON in message (no silent swallow)`(): Unit =
        runBlocking {
            // This is the exact format the old code tried to parse as InvokeError.
            // After the fix, the body excerpt must appear in the exception message.
            val invokeErrorBody = """{"message":"Identifier STATE_abc already in use."}"""
            val requester = buildRequesterWithBody(invokeErrorBody)
            val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

            assertThatThrownBy {
                runBlocking { requester.invoke(dummySignedCmd(chaincodeUri)) }
            }
                .isInstanceOf(InvokeException::class.java)
                .hasMessageContaining("Identifier STATE_abc already in use.")
        }

    @Test
    fun `handleResponse preserves the original parse exception as cause`(): Unit = runBlocking {
        val requester = buildRequesterWithBody("not valid json")
        val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

        assertThatThrownBy {
            runBlocking { requester.invoke(dummySignedCmd(chaincodeUri)) }
        }
            .isInstanceOf(InvokeException::class.java)
            .hasCauseInstanceOf(Exception::class.java)
    }
}
