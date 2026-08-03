package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbSsmGetQuery

class CouchdbSsmGetQueryFunctionImplUnitTest {

	@Test
	suspend fun `returns the ssm and its uri when it exists`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.findResultsByDb["sandbox_ssm"] = """
			{"docs":[{"_id":"s1","docType":"ssm","name":"CarDealership","transitions":[]}]}
		""".trimIndent()
		val function = CouchdbSsmGetQueryFunctionImpl(client)

		val result = function.invoke(
			CouchdbSsmGetQuery(channelId = "sandbox", chaincodeId = "ssm", ssmName = "CarDealership")
		)

		assertThat(result.item?.name).isEqualTo("CarDealership")
		assertThat(result.uri.uri).contains("sandbox").contains("ssm").contains("CarDealership")
		assertThat(cloudant.lastFindOptions?.db()).isEqualTo("sandbox_ssm")
	}

	@Test
	suspend fun `returns a null item when the ssm is unknown`() {
		val (client, _) = fakeCouchdbClient()
		val function = CouchdbSsmGetQueryFunctionImpl(client)

		val result = function.invoke(
			CouchdbSsmGetQuery(channelId = "sandbox", chaincodeId = "ssm", ssmName = "Unknown")
		)

		assertThat(result.item).isNull()
	}
}
