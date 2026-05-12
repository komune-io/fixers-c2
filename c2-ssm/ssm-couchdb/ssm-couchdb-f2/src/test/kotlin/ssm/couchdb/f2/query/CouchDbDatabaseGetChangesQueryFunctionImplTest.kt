package ssm.couchdb.f2.query

import f2.dsl.fnc.invokeWith
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.couchdb.bdd.TestConfig
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQuery
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryFunction

internal class CouchDbDatabaseGetChangesQueryFunctionImplTest: FunctionTestBase() {

	var fnc: CouchdbDatabaseGetChangesQueryFunction = queries.couchdbDatabaseGetChangesQueryFunction()

	@Test
	suspend fun `should return all certificates changes`() {
		val result = CouchdbDatabaseGetChangesQuery(
			chaincodeId = TestConfig.CHAINCODE_ID,
			channelId = TestConfig.CHANNEL_ID,
			ssmName = "Certificates",
			sessionName = null,
			lastEventId = null,
			limit =  null,
		).invokeWith(fnc)

		Assertions.assertThat(result.items).hasSize(0)
	}

	@Test
	suspend fun `should return five certificates changes`() {
		val result = CouchdbDatabaseGetChangesQuery(
			chaincodeId = TestConfig.CHAINCODE_ID,
			channelId = TestConfig.CHANNEL_ID,
			ssmName = "Certificates",
			sessionName = null,
			lastEventId = null,
			limit =  5,
		).invokeWith(fnc)

		Assertions.assertThat(result.items).hasSize(0)
	}

	@Test
	suspend fun `should get session changes`() {
		val result = CouchdbDatabaseGetChangesQuery(
			chaincodeId = TestConfig.CHAINCODE_ID,
			channelId = TestConfig.CHANNEL_ID,
			ssmName = "Certificates",
			sessionName = "certificates-session-4-bed2809b-d60c-40cb-8131-10e625afd065",
			lastEventId = null,
			limit =  null,
		).invokeWith(fnc)

		Assertions.assertThat(result.items).hasSize(0)
	}
}
