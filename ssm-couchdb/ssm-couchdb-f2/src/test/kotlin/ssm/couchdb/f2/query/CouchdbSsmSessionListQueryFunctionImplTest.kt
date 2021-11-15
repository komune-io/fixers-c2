package ssm.couchdb.f2.query

import f2.dsl.fnc.invokeWith
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.uri.ChaincodeUriBurst
import ssm.chaincode.dsl.model.uri.compact
import ssm.couchdb.bdd.TestConfig
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQuery
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryFunction

internal class CouchdbSsmSessionListQueryFunctionImplTest : FunctionTestBase() {

	var couchdbSsmSessionListQueryFunction: CouchdbSsmSessionStateListQueryFunction
		= queries.couchdbSsmSessionStateListQueryFunction()

	@Test
	fun `must return all sessions`(): Unit = runBlocking {
		val sessions = CouchdbSsmSessionStateListQuery(
			chaincodeUri = ChaincodeUriBurst(
				channelId = TestConfig.CHANNEL_ID,
				chaincodeId = TestConfig.CHAINCODE_ID,
			).compact(),
			ssm = null,
			pagination = null
		).invokeWith(couchdbSsmSessionListQueryFunction)
		Assertions.assertThat(sessions.page.list).isNotNull
	}
}
