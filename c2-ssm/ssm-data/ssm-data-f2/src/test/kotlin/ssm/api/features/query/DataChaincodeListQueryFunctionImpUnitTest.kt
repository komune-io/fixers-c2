package ssm.api.features.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import kotlinx.coroutines.flow.map
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryFunction
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryResult
import ssm.data.dsl.features.query.DataChaincodeListQuery

class DataChaincodeListQueryFunctionImpUnitTest {

	@Test
	suspend fun `returns the bursted chaincode uris`() {
		val couchdbFunction = CouchdbChaincodeListQueryFunction { msgs ->
			msgs.map {
				CouchdbChaincodeListQueryResult(
					items = listOf(
						ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm"),
						ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ex02"),
					)
				)
			}
		}
		val function = DataChaincodeListQueryFunctionImp(couchdbFunction)

		val result = function.invoke(DataChaincodeListQuery())

		assertThat(result.items).hasSize(2)
		assertThat(result.items.map { it.chaincodeId }).containsExactly("ssm", "ex02")
		assertThat(result.items.map { it.channelId }).containsOnly("sandbox")
	}

	@Test
	suspend fun `returns an empty list when no chaincode is registered`() {
		val couchdbFunction = CouchdbChaincodeListQueryFunction { msgs ->
			msgs.map { CouchdbChaincodeListQueryResult(items = emptyList()) }
		}
		val function = DataChaincodeListQueryFunctionImp(couchdbFunction)

		val result = function.invoke(DataChaincodeListQuery())

		assertThat(result.items).isEmpty()
	}
}
