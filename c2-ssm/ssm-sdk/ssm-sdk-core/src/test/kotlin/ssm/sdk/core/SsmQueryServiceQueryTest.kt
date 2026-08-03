package ssm.sdk.core

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.sdk.core.ktor.ChaincodeApiGatewayClient
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.json.JSONConverterObjectMapper

class SsmQueryServiceQueryTest {

	private val chaincode = ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm")

	private val agentJson = """{"name":"admin","pub":"cHVi"}"""
	private val ssmJson = """{"name":"CarDealership","transitions":[]}"""
	private val sessionStateJson = """
		{
			"ssm":"CarDealership",
			"session":"deal-1",
			"roles":{"admin":"Seller"},
			"public":"data",
			"origin":{"from":0,"to":1,"role":"Seller","action":"Sell"},
			"current":1,
			"iteration":2
		}
	""".trimIndent()
	private val transactionJson = """
		{
			"transactionId":"tx-1",
			"blockId":4,
			"timestamp":1700000000000,
			"isValid":true,
			"channelId":"sandbox",
			"creator":{"mspid":"msp","id":"identity"},
			"nonce":"bm9uY2U="
		}
	""".trimIndent()
	private val blockJson = """
		{
			"blockId":4,
			"transactions":[],
			"previousHash":"aGFzaA==",
			"dataHash":"aGFzaA=="
		}
	""".trimIndent()
	private val logsJson = """[{"txId":"tx-1","state":$sessionStateJson}]"""

	private fun buildService(responseBody: String): SsmQueryService {
		val engine = MockEngine { _ ->
			respond(
				content = responseBody,
				status = HttpStatusCode.OK,
				headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			)
		}
		val client = HttpClient(engine) {
			install(ContentNegotiation) { jackson() }
		}
		val repository = ChaincodeApiGatewayClient(
			baseUrl = "http://localhost:9090",
			timeout = 5_000L,
			authCredentials = null,
			client = client,
		)
		return SsmQueryService(
			SsmRequester(
				jsonConverter = JSONConverterObjectMapper(),
				ssmChaincodeRepository = repository,
			)
		)
	}

	@Test
	suspend fun `listAdmins returns the admin names`() {
		val result = buildService("""["admin1","admin2"]""").listAdmins(chaincode)
		assertThat(result).containsExactly("admin1", "admin2")
	}

	@Test
	suspend fun `getAdmin returns the parsed agent`() {
		val result = buildService(agentJson).getAdmin(chaincode, "admin")
		assertThat(result?.name).isEqualTo("admin")
	}

	@Test
	suspend fun `listUsers returns the user names`() {
		val result = buildService("""["user1"]""").listUsers(chaincode)
		assertThat(result).containsExactly("user1")
	}

	@Test
	suspend fun `getAgent returns the parsed agent`() {
		val result = buildService(agentJson).getAgent(chaincode, "admin")
		assertThat(result?.name).isEqualTo("admin")
	}

	@Test
	suspend fun `listSsm returns the ssm names`() {
		val result = buildService("""["CarDealership"]""").listSsm(chaincode)
		assertThat(result).containsExactly("CarDealership")
	}

	@Test
	suspend fun `getSsm returns the parsed ssm`() {
		val result = buildService(ssmJson).getSsm(chaincode, "CarDealership")
		assertThat(result?.name).isEqualTo("CarDealership")
		assertThat(result?.transitions).isEmpty()
	}

	@Test
	suspend fun `getSession returns the parsed session state`() {
		val result = buildService(sessionStateJson).getSession(chaincode, "deal-1")
		assertThat(result?.session).isEqualTo("deal-1")
		assertThat(result?.iteration).isEqualTo(2)
	}

	@Test
	suspend fun `log returns the session logs`() {
		val result = buildService(logsJson).log(chaincode, "deal-1")
		assertThat(result).hasSize(1)
		assertThat(result.first().txId).isEqualTo("tx-1")
	}

	@Test
	suspend fun `listSession returns the session names`() {
		val result = buildService("""["deal-1","deal-2"]""").listSession(chaincode)
		assertThat(result).containsExactly("deal-1", "deal-2")
	}

	@Test
	suspend fun `listTransactions returns the transaction ids`() {
		val result = buildService("""["tx-1"]""").listTransactions(chaincode)
		assertThat(result).containsExactly("tx-1")
	}

	@Test
	suspend fun `getTransaction returns the parsed transaction`() {
		val result = buildService(transactionJson).getTransaction(chaincode, "tx-1")
		assertThat(result?.transactionId).isEqualTo("tx-1")
		assertThat(result?.blockId).isEqualTo(4)
	}

	@Test
	suspend fun `listBlocks returns the block ids`() {
		val result = buildService("""[0,1,2]""").listBlocks(chaincode)
		assertThat(result).containsExactly(0, 1, 2)
	}

	@Test
	suspend fun `getBlock returns the parsed block`() {
		val result = buildService(blockJson).getBlock(chaincode, 4)
		assertThat(result?.blockId).isEqualTo(4)
		assertThat(result?.transactions).isEmpty()
	}

	@Test
	suspend fun `getAdmins returns every parsed agent`() {
		val result = buildService(agentJson).getAdmins(
			listOf(GetAdminQuery(chaincode, "admin"), GetAdminQuery(chaincode, "admin"))
		)
		assertThat(result).hasSize(2)
		assertThat(result.map { it.name }).containsOnly("admin")
	}

	@Test
	suspend fun `getAgents returns every parsed agent`() {
		val result = buildService(agentJson).getAgents(listOf(GetAgentQuery(chaincode, "admin")))
		assertThat(result).hasSize(1)
	}

	@Test
	suspend fun `getSsms returns every parsed ssm`() {
		val result = buildService(ssmJson).getSsms(listOf(GetSsmQuery(chaincode, "CarDealership")))
		assertThat(result.map { it.name }).containsExactly("CarDealership")
	}

	@Test
	suspend fun `getSessions returns every parsed session state`() {
		val result = buildService(sessionStateJson).getSessions(listOf(GetSessionQuery(chaincode, "deal-1")))
		assertThat(result).hasSize(1)
		assertThat(result.first()?.session).isEqualTo("deal-1")
	}

	@Test
	suspend fun `getTransactions returns every parsed transaction`() {
		val result = buildService(transactionJson).getTransactions(listOf(GetTransactionQuery(chaincode, "tx-1")))
		assertThat(result).hasSize(1)
		assertThat(result.first()?.transactionId).isEqualTo("tx-1")
	}

	@Test
	suspend fun `getBlocks returns every parsed block`() {
		val result = buildService(blockJson).getBlocks(listOf(GetBlockQuery(chaincode, 4)))
		assertThat(result.map { it.blockId }).containsExactly(4)
	}

	@Test
	suspend fun `getLogs returns the logs of every session`() {
		val result = buildService(logsJson).getLogs(listOf(GetLogQuery(chaincode, "deal-1")))
		assertThat(result).hasSize(1)
		assertThat(result.first().first().txId).isEqualTo("tx-1")
	}
}
