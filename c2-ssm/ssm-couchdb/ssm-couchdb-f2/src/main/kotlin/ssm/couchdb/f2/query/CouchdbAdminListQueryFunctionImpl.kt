package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbAdminListQueryDTO
import ssm.couchdb.dsl.query.CouchdbAdminListQueryFunction
import ssm.couchdb.dsl.query.CouchdbAdminListQueryResult
import ssm.couchdb.dsl.query.CouchdbAdminListQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbAdminListQueryFunctionImpl(
    private val couchdbClient: CouchdbSsmClient,
) : CouchdbAdminListQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309")
    override suspend fun invoke(
        msgs: Flow<CouchdbAdminListQueryDTO>
    ): Flow<CouchdbAdminListQueryResultDTO> = msgs.map { payload ->
        val dbName = payload.chaincodeUri.chainCodeDbName()
        couchdbClient.fetchAllByDocType(dbName, DocType.Admin)
            .let {
                CouchdbAdminListQueryResult(it)
            }
    }
}
