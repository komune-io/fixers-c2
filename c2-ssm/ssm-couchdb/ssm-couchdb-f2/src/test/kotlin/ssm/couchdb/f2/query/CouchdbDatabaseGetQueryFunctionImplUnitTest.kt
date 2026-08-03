package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQuery

class CouchdbDatabaseGetQueryFunctionImplUnitTest {

	@Test
	suspend fun `returns the database when it exists`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.databaseInformationJson = """{"db_name":"sandbox_ssm"}"""
		val function = CouchdbDatabaseGetQueryFunctionImpl(client)

		val result = function.invoke(CouchdbDatabaseGetQuery(channelId = "sandbox", chaincodeId = "ssm"))

		assertThat(result.item?.name).isEqualTo("sandbox_ssm")
	}

	@Test
	suspend fun `returns a null item when the database is missing`() {
		val (client, _) = fakeCouchdbClient()
		val function = CouchdbDatabaseGetQueryFunctionImpl(client)

		val result = function.invoke(CouchdbDatabaseGetQuery(channelId = "sandbox", chaincodeId = "missing"))

		assertThat(result.item).isNull()
	}
}
