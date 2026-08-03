package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbUserListQuery

class CouchdbUserListQueryFunctionImplUnitTest {

	private val chaincodeUri = ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm")

	@Test
	suspend fun `returns the users stored in the chaincode database`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.findResultsByDb["sandbox_ssm"] = """
			{"docs":[{"_id":"u1","docType":"user","name":"user1","pub":"cHVi"}]}
		""".trimIndent()
		val function = CouchdbUserListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbUserListQuery(chaincodeUri))

		assertThat(result.items.map { it.name }).containsExactly("user1")
		assertThat(cloudant.lastFindOptions?.db()).isEqualTo("sandbox_ssm")
	}

	@Test
	suspend fun `returns an empty list when the database has no user`() {
		val (client, _) = fakeCouchdbClient()
		val function = CouchdbUserListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbUserListQuery(chaincodeUri))

		assertThat(result.items).isEmpty()
	}
}
