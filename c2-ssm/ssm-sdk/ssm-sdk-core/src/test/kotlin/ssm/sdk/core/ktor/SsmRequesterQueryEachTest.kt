package ssm.sdk.core.ktor

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
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
import ssm.sdk.core.invoke.query.SsmQueryName
import ssm.sdk.dsl.InvokeException
import ssm.sdk.json.JSONConverterObjectMapper
import tools.jackson.core.type.TypeReference

class SsmRequesterQueryEachTest {

	private data class FakeAgent(val name: String? = null, val pub: String? = null)

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
			coopRepository = repository,
		)
	}

	private fun apiQueries(vararg names: String): List<SsmApiQuery> = names.map { name ->
		SsmApiQuery(
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			value = name,
			query = StubGetQuery(SsmQueryName.USER),
		)
	}

	private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

	@Test
	fun `queryEach preserves input order including nulls`(): Unit = runBlocking {
		val requester = buildRequester { request ->
			val argValue = request.url.parameters["args"]
			val body = when (argValue) {
				"alice" -> """{"name":"alice","pub":"key-a"}"""
				"bob" -> "null"
				"carol" -> """{"name":"carol","pub":"key-c"}"""
				else -> error("unexpected args=$argValue")
			}
			respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders())
		}

		val results = requester.queryEach(
			apiQueries("alice", "bob", "carol"),
			object : TypeReference<FakeAgent?>() {},
		)

		assertThat(results).hasSize(3)
		assertThat(results[0]).isEqualTo(FakeAgent(name = "alice", pub = "key-a"))
		assertThat(results[1]).isNull()
		assertThat(results[2]).isEqualTo(FakeAgent(name = "carol", pub = "key-c"))
	}

	@Test
	fun `queryEach propagates HTTP 500 from any single query`() {
		val requester = buildRequester { request ->
			if (request.url.parameters["args"] == "bob") {
				respondError(HttpStatusCode.InternalServerError)
			} else {
				respond(content = """{"name":"alice"}""", status = HttpStatusCode.OK, headers = jsonHeaders())
			}
		}

		assertThatThrownBy {
			runBlocking {
				requester.queryEach(
					apiQueries("alice", "bob"),
					object : TypeReference<FakeAgent?>() {},
				)
			}
		}.isInstanceOfAny(
			RuntimeException::class.java,
			InvokeException::class.java,
		)
	}
}
