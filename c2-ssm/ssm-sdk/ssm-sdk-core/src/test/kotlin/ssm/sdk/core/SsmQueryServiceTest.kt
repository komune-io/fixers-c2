package ssm.sdk.core

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ssm.sdk.core.ktor.ChaincodeApiGatewayClient
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.dsl.InvokeException
import ssm.sdk.json.JSONConverterObjectMapper

/**
 * Unit tests for SsmQueryService backed by a real SsmRequester over a Ktor MockEngine —
 * no live Fabric/gateway required. Covers the single-value query path, the list path,
 * the queryLogs path and the batched queryEachOf fan-out (both the `.filterNotNull()`
 * variants and the null-preserving variants — the difference is deliberate, see
 * SsmQueryService).
 */
class SsmQueryServiceTest {

	private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

	private val sessionStateJson = """
		{
		  "ssm": "ssmName1",
		  "session": "session-1",
		  "roles": {"alice": "Buyer"},
		  "public": "some-public-state",
		  "private": {},
		  "origin": null,
		  "current": 2,
		  "iteration": 3
		}
	""".trimIndent()

	private val sessionLogsJson = """
		[
		  {
		    "txId": "tx-42",
		    "state": $sessionStateJson
		  }
		]
	""".trimIndent()

	private val agentJson = """{"name":"alice","pub":"QUJD"}"""

	private fun buildRequester(handler: MockRequestHandler): SsmRequester {
		val client = HttpClient(MockEngine(handler)) {
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
			ssmChaincodeRepository = repository,
		)
	}

	private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

	private fun respondingWith(body: (fcn: String?, args: String?) -> String): SsmQueryService {
		val requester = buildRequester { request ->
			respond(
				content = body(request.url.parameters["fcn"], request.url.parameters["args"]),
				status = HttpStatusCode.OK,
				headers = jsonHeaders(),
			)
		}
		return SsmQueryService(requester)
	}

	// ------------------------------------------------------------------
	// list path (SsmRequester.list)
	// ------------------------------------------------------------------

	@Test
	suspend fun `list queries return parsed name lists`() {
		val service = respondingWith { fcn, _ ->
			if (fcn == "list") """["one","two"]""" else "null"
		}

		assertThat(service.listAdmins(chaincodeUri)).containsExactly("one", "two")
		assertThat(service.listUsers(chaincodeUri)).containsExactly("one", "two")
		assertThat(service.listSsm(chaincodeUri)).containsExactly("one", "two")
		assertThat(service.listSession(chaincodeUri)).containsExactly("one", "two")
		assertThat(service.listTransactions(chaincodeUri)).containsExactly("one", "two")
	}

	@Test
	suspend fun `listBlocks returns parsed block ids`() {
		val service = respondingWith { fcn, _ ->
			if (fcn == "list") "[1,2,3]" else "null"
		}

		assertThat(service.listBlocks(chaincodeUri)).containsExactly(1, 2, 3)
	}

	@Test
	fun `list wraps an unparseable response into InvokeException with a response excerpt`() {
		val service = respondingWith { _, _ -> "{not-a-json-array" }

		assertThatThrownBy { runBlocking { service.listAdmins(chaincodeUri) } }
			.isInstanceOf(InvokeException::class.java)
			.hasMessageContaining("Error parsing response: {not-a-json-array")
	}

	// ------------------------------------------------------------------
	// single query path (SsmRequester.query)
	// ------------------------------------------------------------------

	@Test
	suspend fun `single get queries return null when the chain has no entry`() {
		val service = respondingWith { _, _ -> "null" }

		assertThat(service.getAdmin(chaincodeUri, "ghost")).isNull()
		assertThat(service.getAgent(chaincodeUri, "ghost")).isNull()
		assertThat(service.getSsm(chaincodeUri, "ghost")).isNull()
		assertThat(service.getTransaction(chaincodeUri, "ghost-tx")).isNull()
		assertThat(service.getBlock(chaincodeUri, 7)).isNull()
	}

	@Test
	suspend fun `getSession maps the chain session state`() {
		val service = respondingWith { _, _ -> sessionStateJson }

		val state = service.getSession(chaincodeUri, "session-1")

		assertThat(state).isNotNull
		assertThat(state!!.session).isEqualTo("session-1")
		assertThat(state.iteration).isEqualTo(3)
		assertThat(state.roles).containsEntry("alice", "Buyer")
	}

	// ------------------------------------------------------------------
	// logs path (SsmRequester.queryLogs + deprecated alias)
	// ------------------------------------------------------------------

	@Test
	suspend fun `log returns the session state logs`() {
		val service = respondingWith { _, _ -> sessionLogsJson }

		val logs = service.log(chaincodeUri, "session-1")

		assertThat(logs).hasSize(1)
		assertThat(logs[0].txId).isEqualTo("tx-42")
		assertThat(logs[0].state.iteration).isEqualTo(3)
	}

	// ------------------------------------------------------------------
	// batched queryEachOf path
	// ------------------------------------------------------------------

	@Test
	suspend fun `getAdmins and getAgents drop missing entries`() {
		val service = respondingWith { _, args ->
			if (args == "alice") agentJson else "null"
		}

		val admins = service.getAdmins(
			listOf(GetAdminQuery(chaincodeUri, "alice"), GetAdminQuery(chaincodeUri, "ghost"))
		)
		val agents = service.getAgents(
			listOf(GetAgentQuery(chaincodeUri, "alice"), GetAgentQuery(chaincodeUri, "ghost"))
		)

		assertThat(admins).hasSize(1)
		assertThat(admins[0].name).isEqualTo("alice")
		assertThat(agents).hasSize(1)
		assertThat(agents[0].name).isEqualTo("alice")
	}

	@Test
	suspend fun `getSsms and getBlocks drop missing entries`() {
		val service = respondingWith { _, _ -> "null" }

		assertThat(service.getSsms(listOf(GetSsmQuery(chaincodeUri, "ghost")))).isEmpty()
		assertThat(service.getBlocks(listOf(GetBlockQuery(chaincodeUri, 7)))).isEmpty()
	}

	@Test
	suspend fun `getSessions preserves per-query nulls in order`() {
		val service = respondingWith { _, args ->
			if (args == "session-1") sessionStateJson else "null"
		}

		val sessions = service.getSessions(
			listOf(GetSessionQuery(chaincodeUri, "session-1"), GetSessionQuery(chaincodeUri, "ghost"))
		)

		assertThat(sessions).hasSize(2)
		assertThat(sessions[0]?.session).isEqualTo("session-1")
		assertThat(sessions[1]).isNull()
	}

	@Test
	suspend fun `getTransactions preserves per-query nulls`() {
		val service = respondingWith { _, _ -> "null" }

		val transactions = service.getTransactions(
			listOf(GetTransactionQuery(chaincodeUri, "tx-1"), GetTransactionQuery(chaincodeUri, "tx-2"))
		)

		assertThat(transactions).hasSize(2)
		assertThat(transactions).containsExactly(null, null)
	}

	@Test
	suspend fun `getLogs returns one log list per query`() {
		val service = respondingWith { _, args ->
			if (args == "session-1") sessionLogsJson else "[]"
		}

		val logs = service.getLogs(
			listOf(GetLogQuery(chaincodeUri, "session-1"), GetLogQuery(chaincodeUri, "ghost"))
		)

		assertThat(logs).hasSize(2)
		assertThat(logs[0]).hasSize(1)
		assertThat(logs[0][0].txId).isEqualTo("tx-42")
		assertThat(logs[1]).isEmpty()
	}
}
