package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbSsmListQuery
import ssm.couchdb.dsl.query.CouchdbSsmListQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmListQueryResult
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbSsmListQueryFunctionImpl(
    private val couchdbClient: CouchdbSsmClient,
) : CouchdbSsmListQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(
        msgs: Flow<CouchdbSsmListQuery>
    ): Flow<CouchdbSsmListQueryResult> = msgs.map { payload ->
        val dbName = chainCodeDbName(channelId = payload.channelId, chaincodeId = payload.chaincodeId)
        val pagination = payload.pagination
        val items = couchdbClient.fetchAllByDocType(
            dbName = dbName,
            docType = DocType.Ssm,
            limit = pagination?.run { limit.toLong() },
            skip = pagination?.run { offset.toLong() },
        )
        CouchdbSsmListQueryResult(
            items = items,
            // Without a requested page everything was fetched, so the item count is the total.
            total = pagination?.let { couchdbClient.countByDocType(dbName, DocType.Ssm) } ?: items.size,
            pagination = pagination
        )
    }
}
