package ssm.couchdb.f2.query

import f2.dsl.fnc.invokeWith
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.couchdb.bdd.TestConfig
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQuery
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryFunction

internal class CouchdbSsmSessionListQueryFunctionImplTest : FunctionTestBase() {

	var couchdbSsmSessionListQueryFunction: CouchdbSsmSessionStateListQueryFunction
		= queries.couchdbSsmSessionStateListQueryFunction()

	@Test
	suspend fun `must return all sessions`() {
		val sessions = CouchdbSsmSessionStateListQuery(
			chaincodeUri = ChaincodeUri.from(
				channelId = TestConfig.CHANNEL_ID,
				chaincodeId = TestConfig.CHAINCODE_ID,
			),
			ssm = null,
			pagination = null
		).invokeWith(couchdbSsmSessionListQueryFunction)
		Assertions.assertThat(sessions.items).isNotNull
	}
}
