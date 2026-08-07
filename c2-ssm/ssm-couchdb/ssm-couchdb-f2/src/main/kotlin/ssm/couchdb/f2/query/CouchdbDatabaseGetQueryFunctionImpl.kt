package ssm.couchdb.f2.query

import com.ibm.cloud.cloudant.v1.model.DatabaseInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
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

	private val logger = LoggerFactory.getLogger(CouchdbDatabaseGetQueryFunctionImpl::class.java)

	private fun DatabaseInformation.asDatabase() = Database(this.dbName)

	@Suppress("TooGenericExceptionCaught")
	override suspend fun invoke(
		msgs: Flow<CouchdbDatabaseGetQueryDTO>
	): Flow<CouchdbDatabaseGetQueryResultDTO> = msgs.map { payload ->
		val dbName = chainCodeDbName(payload.channelId, payload.chaincodeId)
		try {
			CouchdbDatabaseGetQueryResult(
				item = couchdbClient.getDatabase(dbName).asDatabase()
			)
		} catch (e: Exception) {
			logger.error("Failed to get database $dbName", e)
			CouchdbDatabaseGetQueryResult(
				item = null
			)
		}
	}
}
