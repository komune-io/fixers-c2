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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.dsl.isRetryable
import ssm.sdk.dsl.isSuccess
import ssm.sdk.dsl.isPermanent
import ssm.sdk.json.JSONConverterObjectMapper

class SsmRequesterTest {

    private fun buildMockRequester(responseBody: String): SsmRequester {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val mockHttpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                jackson()
            }
        }
        val repository = ChaincodeApiGatewayClient(
            baseUrl = "http://localhost:9090",
            timeout = 5_000L,
            authCredentials = null,
            client = mockHttpClient,
        )
        return SsmRequester(
            jsonConverter = JSONConverterObjectMapper(),
            ssmChaincodeClient = repository,
        )
    }

    private fun buildSignedCmd(chaincodeUri: ChaincodeUri): SsmCmdSigned {
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

    private fun ceItem(
        subject: String,
        type: String,
        data: String,
    ): String = """
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

    @Test
    fun `invokeAll deserializes mixed CE outcomes without throwing`(): Unit = runBlocking {
        val responseJson = "[" + ceItem(
            subject = "cmd-1",
            type = "io.komune.c2.invoke.outcome.committed",
            data = """{"transactionId":"tx-abc123","blockNumber":42}""",
        ) + "," + ceItem(
            subject = "cmd-2",
            type = "io.komune.c2.invoke.outcome.rejected",
            data = """{"errorCode":"MVCC_READ_CONFLICT","errorMessage":"conflict on key session-xyz"}""",
        ) + "]"

        val requester = buildMockRequester(responseJson)
        val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
        val cmds = listOf(
            buildSignedCmd(chaincodeUri),
            buildSignedCmd(chaincodeUri),
        )
        val commandIds = listOf("cmd-1", "cmd-2")

        val outcomes: List<CommandOutcome> = requester.invokeAll(cmds, commandIds)

        assertThat(outcomes).hasSize(2)

        val first = outcomes[0]
        assertThat(first.outcome).isEqualTo("Committed")
        assertThat(first.msgId).isEqualTo("cmd-1")
        assertThat(first.transactionId).isEqualTo("tx-abc123")
        assertThat(first.blockNumber).isEqualTo(42L)
        assertThat(first.isSuccess).isTrue()

        val second = outcomes[1]
        assertThat(second.outcome).isEqualTo("Rejected")
        assertThat(second.msgId).isEqualTo("cmd-2")
        assertThat(second.errorCode).isEqualTo("MVCC_READ_CONFLICT")
        assertThat(second.errorMessage).isEqualTo("conflict on key session-xyz")
        assertThat(second.isPermanent).isTrue()
        assertThat(second.isSuccess).isFalse()
    }

    @Test
    fun `invokeAll size-mismatch guard fires before any HTTP call`() {
        val requester = buildMockRequester("[]")
        val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
        val cmds = listOf(buildSignedCmd(chaincodeUri))
        val commandIds = listOf("cmd-1", "cmd-2")

        assertThatThrownBy {
            runBlocking { requester.invokeAll(cmds, commandIds) }
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("msgIds.size=2 must match cmds.size=1")
    }

    @Test
    fun `invokeAll handles Transient outcome with isRetryable predicate`(): Unit = runBlocking {
        val responseJson = "[" + ceItem(
            subject = "cmd-retry",
            type = "io.komune.c2.invoke.outcome.transient",
            data = "{}",
        ) + "]"

        val requester = buildMockRequester(responseJson)
        val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
        val cmds = listOf(buildSignedCmd(chaincodeUri))
        val commandIds = listOf("cmd-retry")

        val outcomes = requester.invokeAll(cmds, commandIds)

        assertThat(outcomes).hasSize(1)
        val outcome = outcomes[0]
        assertThat(outcome.isRetryable).isTrue()
        assertThat(outcome.isSuccess).isFalse()
    }
}
