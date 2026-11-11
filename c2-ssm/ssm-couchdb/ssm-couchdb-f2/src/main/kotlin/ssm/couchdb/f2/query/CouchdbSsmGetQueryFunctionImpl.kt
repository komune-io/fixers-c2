package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.from
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbSsmGetQuery
import ssm.couchdb.dsl.query.CouchdbSsmGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmGetQueryResult
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbSsmGetQueryFunctionImpl(
    private val couchdbClient: CouchdbSsmClient,
) : CouchdbSsmGetQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(
        msgs: Flow<CouchdbSsmGetQuery>
    ): Flow<CouchdbSsmGetQueryResult> = msgs.map { payload ->
        couchdbClient
            .fetchOneByDocTypeAndName(
                dbName = chainCodeDbName(channelId = payload.channelId, chaincodeId = payload.chaincodeId),
                docType = DocType.Ssm,
                name = payload.ssmName,
            )
            .let { item ->
                CouchdbSsmGetQueryResult(
                    item = item,
                    uri = SsmUri.from(
                        channelId = payload.channelId,
                        chaincodeId = payload.chaincodeId,
                        ssmName = payload.ssmName,
                    )
                )
            }
    }
}
