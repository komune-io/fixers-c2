package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbChaincodeListQuery

class CouchdbChaincodeListQueryFunctionImplUnitTest {

	@Test
	suspend fun `lists the chaincodes registered in lscc databases`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.allDbsResult = listOf("sandbox_lscc", "sandbox_ssm")
		cloudant.findResultsByDb["sandbox_lscc"] = """
			{"docs":[{"_id":"ssm","_rev":"1-abc"},{"_id":"ex02","_rev":"1-def"}]}
		""".trimIndent()
		val function = CouchdbChaincodeListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbChaincodeListQuery())

		assertThat(result.items).containsExactlyInAnyOrder(
			ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm"),
			ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ex02"),
		)
	}

	@Test
	suspend fun `returns an empty list when no lscc database exists`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.allDbsResult = listOf("sandbox_ssm")
		val function = CouchdbChaincodeListQueryFunctionImpl(client)

		val result = function.invoke(CouchdbChaincodeListQuery())

		assertThat(result.items).isEmpty()
	}
}
