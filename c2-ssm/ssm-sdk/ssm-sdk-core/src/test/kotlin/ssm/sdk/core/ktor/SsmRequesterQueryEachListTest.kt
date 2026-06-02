package ssm.sdk.core.ktor

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
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.sdk.core.invoke.query.SsmQueryName
import ssm.sdk.json.JSONConverterObjectMapper
import tools.jackson.core.type.TypeReference

class SsmRequesterQueryEachListTest {

	private data class FakeLog(val txId: String, val iteration: Int)

	private fun buildRequester(handler: MockRequestHandler): SsmRequester {
		val engine = MockEngine(handler)
		val client = HttpClient(engine) {
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

	private fun apiQueries(vararg sessions: String): List<SsmApiQuery> = sessions.map { name ->
		SsmApiQuery(
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			value = name,
			query = StubGetQuery(SsmQueryName.LOG),
		)
	}

	private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

	@Test
	fun `queryEachList keeps one inner list per query and preserves order and sizes`(): Unit = runBlocking {
		val requester = buildRequester { request ->
			val argValue = request.url.parameters["args"]
			val body = when (argValue) {
				"session-A" -> """[{"txId":"tx1","iteration":0},{"txId":"tx2","iteration":1},{"txId":"tx3","iteration":2}]"""
				"session-B" -> """[{"txId":"tx9","iteration":7}]"""
				else -> error("unexpected args=$argValue")
			}
			respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders())
		}

		val results = requester.queryEachList(
			apiQueries("session-A", "session-B"),
			object : TypeReference<List<FakeLog>>() {},
		)

		assertThat(results).hasSize(2)
		assertThat(results[0]).hasSize(3)
		assertThat(results[1]).hasSize(1)
		assertThat(results[0][0].txId).isEqualTo("tx1")
		assertThat(results[1][0].txId).isEqualTo("tx9")
	}

	@Test
	fun `queryEachList preserves empty list slot without dropping it`(): Unit = runBlocking {
		val requester = buildRequester { request ->
			val argValue = request.url.parameters["args"]
			val body = when (argValue) {
				"session-empty" -> "[]"
				"session-full" -> """[{"txId":"tx1","iteration":0}]"""
				else -> error("unexpected args=$argValue")
			}
			respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders())
		}

		val results = requester.queryEachList(
			apiQueries("session-empty", "session-full"),
			object : TypeReference<List<FakeLog>>() {},
		)

		assertThat(results).hasSize(2)
		assertThat(results[0]).isEmpty()
		assertThat(results[1]).hasSize(1)
	}

	@Test
	fun `queryEachList does not flatten across queries`(): Unit = runBlocking {
		val requester = buildRequester { _ ->
			respond(
				content = """[{"txId":"tx","iteration":0},{"txId":"tx","iteration":1}]""",
				status = HttpStatusCode.OK,
				headers = jsonHeaders(),
			)
		}

		val results = requester.queryEachList(
			apiQueries("session-1", "session-2"),
			object : TypeReference<List<FakeLog>>() {},
		)

		assertThat(results).hasSize(2)
		assertThat(results.flatten()).hasSize(4)
	}
}
