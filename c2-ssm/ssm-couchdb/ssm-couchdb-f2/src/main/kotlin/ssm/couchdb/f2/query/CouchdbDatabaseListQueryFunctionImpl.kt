package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.Database
import ssm.couchdb.dsl.query.CouchdbDatabaseListQueryDTO
import ssm.couchdb.dsl.query.CouchdbDatabaseListQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseListQueryResult
import ssm.couchdb.dsl.query.CouchdbDatabaseListQueryResultDTO

class CouchdbDatabaseListQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbDatabaseListQueryFunction {

	override suspend fun invoke(
		msgs: Flow<CouchdbDatabaseListQueryDTO>
	): Flow<CouchdbDatabaseListQueryResultDTO> = msgs.map { payload ->
		val total = 0
		val filter = listOfNotNull(payload.channelId, "_", payload.chaincodeId).joinToString()

		couchdbClient.getDatabases()
			.asFlow()
			.filter { it.contains(filter) }
			.toDatabases()
			.let { list ->
				CouchdbDatabaseListQueryResult(
					total = total,
					items = list,
					pagination = payload.pagination
				)
			}
	}

	private suspend fun Flow<String>.toDatabases(): List<Database> =
		map(::Database).toList()
}
