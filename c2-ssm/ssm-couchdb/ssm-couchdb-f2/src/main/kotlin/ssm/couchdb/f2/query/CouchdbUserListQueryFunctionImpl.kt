package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbUserListQueryDTO
import ssm.couchdb.dsl.query.CouchdbUserListQueryFunction
import ssm.couchdb.dsl.query.CouchdbUserListQueryResult
import ssm.couchdb.dsl.query.CouchdbUserListQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbUserListQueryFunctionImpl(
    private val couchdbClient: CouchdbSsmClient,
) : CouchdbUserListQueryFunction {

    override suspend fun invoke(msgs: Flow<CouchdbUserListQueryDTO>): Flow<CouchdbUserListQueryResultDTO> =
        msgs.map { payload ->
            val dbName = payload.chaincodeUri.chainCodeDbName()
            couchdbClient.fetchAllByDocType(dbName, DocType.User)
                .let {
                    CouchdbUserListQueryResult(it)
                }
        }
}
