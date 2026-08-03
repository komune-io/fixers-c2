package ssm.chaincode.f2.query

import f2.dsl.fnc.invoke
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
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.query.SsmGetAdminQuery
import ssm.chaincode.dsl.query.SsmGetQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionQuery
import ssm.chaincode.dsl.query.SsmGetTransactionQuery
import ssm.chaincode.dsl.query.SsmGetUserQuery
import ssm.chaincode.dsl.query.SsmListAdminQuery
import ssm.chaincode.dsl.query.SsmListSessionQuery
import ssm.chaincode.dsl.query.SsmListSsmQuery
import ssm.chaincode.dsl.query.SsmListUserQuery
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.ktor.ChaincodeApiGatewayClient
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.json.JSONConverterObjectMapper

class SsmChaincodeQueryFunctionImplsUnitTest {

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

	private fun buildQueryService(responseBody: String): SsmQueryService {
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
	suspend fun `SsmGetAdminFunctionImpl returns the parsed admin`() {
		val function = SsmGetAdminFunctionImpl(buildQueryService(agentJson))
		val result = function.invoke(SsmGetAdminQuery(chaincode, "admin"))
		assertThat(result.item?.name).isEqualTo("admin")
	}

	@Test
	suspend fun `SsmGetUserFunctionImpl returns the parsed user`() {
		val function = SsmGetUserFunctionImpl(buildQueryService(agentJson))
		val result = function.invoke(SsmGetUserQuery(chaincode, "admin"))
		assertThat(result.item?.name).isEqualTo("admin")
	}

	@Test
	suspend fun `SsmGetQueryFunctionImpl returns the parsed ssm`() {
		val function = SsmGetQueryFunctionImpl(buildQueryService(ssmJson))
		val result = function.invoke(SsmGetQuery(chaincode, "CarDealership"))
		assertThat(result.item?.name).isEqualTo("CarDealership")
	}

	@Test
	suspend fun `SsmGetSessionQueryFunctionImpl returns the parsed session`() {
		val function = SsmGetSessionQueryFunctionImpl(buildQueryService(sessionStateJson))
		val result = function.invoke(SsmGetSessionQuery(chaincode, "deal-1"))
		assertThat(result.item?.session).isEqualTo("deal-1")
	}

	@Test
	suspend fun `SsmGetTransactionQueryFunctionImpl returns the parsed transaction`() {
		val function = SsmGetTransactionQueryFunctionImpl(buildQueryService(transactionJson))
		val result = function.invoke(SsmGetTransactionQuery(chaincode, "tx-1"))
		assertThat(result.item?.transactionId).isEqualTo("tx-1")
	}

	@Test
	suspend fun `SsmListAdminQueryFunctionImpl returns the admin names`() {
		val function = SsmListAdminQueryFunctionImpl(buildQueryService("""["admin1","admin2"]"""))
		val result = function.invoke(SsmListAdminQuery(chaincode))
		assertThat(result.items.toList()).containsExactly("admin1", "admin2")
	}

	@Test
	suspend fun `SsmListUserQueryFunctionImpl returns the user names`() {
		val function = SsmListUserQueryFunctionImpl(buildQueryService("""["user1"]"""))
		val result = function.invoke(SsmListUserQuery(chaincode))
		assertThat(result.items.toList()).containsExactly("user1")
	}

	@Test
	suspend fun `SsmListSsmQueryFunctionImpl returns the ssm names`() {
		val function = SsmListSsmQueryFunctionImpl(buildQueryService("""["CarDealership"]"""))
		val result = function.invoke(SsmListSsmQuery(chaincode))
		assertThat(result.items.toList()).containsExactly("CarDealership")
	}

	@Test
	suspend fun `SsmListSessionQueryFunctionImpl returns the session names`() {
		val function = SsmListSessionQueryFunctionImpl(buildQueryService("""["deal-1","deal-2"]"""))
		val result = function.invoke(SsmListSessionQuery(chaincode))
		assertThat(result.items.toList()).containsExactly("deal-1", "deal-2")
	}

	@Test
	suspend fun `SsmGetSessionLogsQueryFunctionImpl returns the logs of the session`() {
		val logsJson = """[{"txId":"tx-1","state":$sessionStateJson}]"""
		val function = SsmGetSessionLogsQueryFunctionImpl(
			SsmBatchProperties(),
			buildQueryService(logsJson),
		)

		val result = function.invoke(
			SsmGetSessionLogsQuery(chaincodeUri = chaincode, ssmName = "CarDealership", sessionName = "deal-1")
		)

		assertThat(result.sessionName).isEqualTo("deal-1")
		assertThat(result.logs.map { it.txId }).containsExactly("tx-1")
	}

	@Test
	suspend fun `SsmGetSessionLogsQueryFunctionImpl returns empty logs for an unknown session`() {
		val function = SsmGetSessionLogsQueryFunctionImpl(
			SsmBatchProperties(),
			buildQueryService("""[]"""),
		)

		val result = function.invoke(
			SsmGetSessionLogsQuery(chaincodeUri = chaincode, ssmName = "CarDealership", sessionName = "unknown")
		)

		assertThat(result.logs).isEmpty()
	}
}
