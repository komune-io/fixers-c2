package ssm.couchdb.f2.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQuery

class CouchdbSsmSessionStateGetQueryFunctionImplUnitTest {

	private val chaincodeUri = ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm")

	@Test
	suspend fun `returns the session state matching the session name`() {
		val (client, cloudant) = fakeCouchdbClient()
		cloudant.findResultsByDb["sandbox_ssm"] = """
			{"docs":[{
				"_id":"st1",
				"docType":"state",
				"ssm":"CarDealership",
				"session":"deal-1",
				"roles":{"admin":"Seller"},
				"public":"public-data",
				"current":1,
				"iteration":3
			}]}
		""".trimIndent()
		val function = CouchdbSsmSessionStateGetQueryFunctionImpl(client)

		val result = function.invoke(
			CouchdbSsmSessionStateGetQuery(
				chaincodeUri = chaincodeUri,
				ssmName = "CarDealership",
				sessionName = "deal-1",
			)
		)

		assertThat(result.item.session).isEqualTo("deal-1")
		assertThat(result.item.iteration).isEqualTo(3)
		assertThat(cloudant.lastFindOptions?.db()).isEqualTo("sandbox_ssm")
	}

	@Test
	suspend fun `fails when no state matches the session name`() {
		val (client, _) = fakeCouchdbClient()
		val function = CouchdbSsmSessionStateGetQueryFunctionImpl(client)

		val exception = runCatching {
			function.invoke(
				CouchdbSsmSessionStateGetQuery(
					chaincodeUri = chaincodeUri,
					ssmName = "CarDealership",
					sessionName = "unknown",
				)
			)
		}.exceptionOrNull()

		assertThat(exception).isInstanceOf(NoSuchElementException::class.java)
	}
}
