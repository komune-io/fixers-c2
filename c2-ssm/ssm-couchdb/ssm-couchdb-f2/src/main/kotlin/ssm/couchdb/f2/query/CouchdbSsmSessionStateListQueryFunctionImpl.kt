package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.SsmSessionStateDTO
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryDTO
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryResult
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbSsmSessionStateListQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbSsmSessionStateListQueryFunction {

	override suspend fun invoke(
		msgs: Flow<CouchdbSsmSessionStateListQueryDTO>
	): Flow<CouchdbSsmSessionStateListQueryResultDTO> =
		msgs.map { payload ->
			val filters = payload.ssm?.let { ssm ->
				mapOf(SsmSessionStateDTO::ssm.name to ssm)
			} ?: emptyMap()
			val dbName = payload.chaincodeUri.chainCodeDbName()
			val pagination = payload.pagination
			val items = couchdbClient.fetchAllByDocType(
				dbName = dbName,
				docType = DocType.State,
				filters = filters,
				limit = pagination?.limit?.toLong(),
				skip = pagination?.offset?.toLong(),
			)
			CouchdbSsmSessionStateListQueryResult(
				items = items,
				// Without a requested page everything was fetched, so the item count is the total.
				total = pagination?.let { couchdbClient.countByDocType(dbName, DocType.State, filters) }
					?: items.size,
				pagination = pagination
			)
		}
}
