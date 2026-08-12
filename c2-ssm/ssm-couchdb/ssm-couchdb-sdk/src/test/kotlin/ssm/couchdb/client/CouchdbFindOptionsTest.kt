package ssm.couchdb.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.model.DocType

/**
 * Pins the `_find` options the client sends to CouchDB. Pure option building: no server needed.
 */
internal class CouchdbFindOptionsTest {

	private fun skipOf(options: com.ibm.cloud.cloudant.v1.model.PostFindOptions): Long? = options.skip()

	private val selector = CouchdbSsmClient.docTypeSelector(DocType.Ssm, emptyMap())

	@Test
	fun `pushes the requested page into limit and skip`() {
		val options = CouchdbSsmClient.buildFindOptions("db", selector, limit = 20, skip = 40)

		assertThat(options.limit()).isEqualTo(20L)
		assertThat(options.skip()).isEqualTo(40L)
		assertThat(options.db()).isEqualTo("db")
	}

	@Test
	fun `keeps fetching everything when no page is requested`() {
		val options = CouchdbSsmClient.buildFindOptions("db", selector)

		assertThat(options.limit()).isEqualTo(Long.MAX_VALUE)
		assertThat(skipOf(options)).isNull()
	}

	@Test
	fun `applies a limit without a skip`() {
		val options = CouchdbSsmClient.buildFindOptions("db", selector, limit = 10)

		assertThat(options.limit()).isEqualTo(10L)
		assertThat(skipOf(options)).isNull()
	}

	@Test
	fun `counts by fetching ids only, over the whole selection`() {
		val options = CouchdbSsmClient.buildFindOptions(
			dbName = "db",
			selector = selector,
			fields = listOf(CouchdbSsmClient.ID_FIELD),
		)

		assertThat(options.fields()).containsExactly("_id")
		assertThat(options.limit()).isEqualTo(Long.MAX_VALUE)
		assertThat(skipOf(options)).isNull()
	}

	@Test
	fun `selects on docType and merges the extra filters`() {
		val withFilters = CouchdbSsmClient.docTypeSelector(DocType.State, mapOf("ssm" to "CarDealership"))

		assertThat(withFilters).containsEntry("docType", mapOf("\$eq" to "state"))
		assertThat(withFilters).containsEntry("ssm", "CarDealership")
		assertThat(selector).hasSize(1).containsEntry("docType", mapOf("\$eq" to "ssm"))
	}
}
