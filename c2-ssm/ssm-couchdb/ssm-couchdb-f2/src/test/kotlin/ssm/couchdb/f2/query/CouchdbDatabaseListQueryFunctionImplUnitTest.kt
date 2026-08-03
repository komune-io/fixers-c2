package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbDatabaseListQuery

class CouchdbDatabaseListQueryFunctionImplUnitTest {

	@Test
	suspend fun `lists the databases matching the filter`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.allDbsResult = listOf("sandbox_ssm", "sandbox_lscc", "plain")
		val function = CouchdbDatabaseListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbDatabaseListQuery())

		assertThat(result.items.map { it.name }).containsExactly("sandbox_ssm", "sandbox_lscc")
		assertThat(result.total).isEqualTo(0)
	}

	@Test
	suspend fun `returns an empty list when no database matches`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.allDbsResult = listOf("plain")
		val function = CouchdbDatabaseListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbDatabaseListQuery())

		assertThat(result.items).isEmpty()
	}
}
