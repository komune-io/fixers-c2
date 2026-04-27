package ssm.couchdb.f2.query

import com.ibm.cloud.cloudant.v1.model.DatabaseInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.Database
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryDTO
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryResult
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbDatabaseGetQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbDatabaseGetQueryFunction {

	private fun DatabaseInformation.asDatabase() = Database(this.dbName)

	@Suppress("TooGenericExceptionCaught")
	override suspend fun invoke(
		msgs: Flow<CouchdbDatabaseGetQueryDTO>
	): Flow<CouchdbDatabaseGetQueryResultDTO> = msgs.map { payload ->
		try {
			CouchdbDatabaseGetQueryResult(
				item = couchdbClient.getDatabase(chainCodeDbName(payload.channelId, payload.chaincodeId)).asDatabase()
			)
		} catch (e: Exception) {
			e.printStackTrace()
			CouchdbDatabaseGetQueryResult(
				item = null
			)
		}
	}
}
