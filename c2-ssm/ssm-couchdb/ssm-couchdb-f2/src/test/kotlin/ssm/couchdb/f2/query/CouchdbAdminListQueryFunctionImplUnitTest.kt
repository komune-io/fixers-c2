package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbAdminListQuery

class CouchdbAdminListQueryFunctionImplUnitTest {

	private val chaincodeUri = ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm")

	@Test
	suspend fun `returns the admins stored in the chaincode database`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.findResultsByDb["sandbox_ssm"] = """
			{"docs":[
				{"_id":"a1","docType":"admin","name":"admin1","pub":"cHVi"},
				{"_id":"a2","docType":"admin","name":"admin2","pub":"cHVi"}
			]}
		""".trimIndent()
		val function = CouchdbAdminListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbAdminListQuery(chaincodeUri))

		assertThat(result.items.map { it.name }).containsExactly("admin1", "admin2")
		assertThat(cloudant.lastFindOptions?.db()).isEqualTo("sandbox_ssm")
	}

	@Test
	suspend fun `returns an empty list when the database has no admin`() {
		val (client, _) = fakeCouchdbClient()
		val function = CouchdbAdminListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbAdminListQuery(chaincodeUri))

		assertThat(result.items).isEmpty()
	}
}
