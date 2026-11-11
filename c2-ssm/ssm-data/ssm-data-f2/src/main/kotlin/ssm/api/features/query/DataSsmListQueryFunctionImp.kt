package ssm.api.features.query

import kotlinx.coroutines.ExperimentalCoroutinesApi
import f2.dsl.fnc.invokeWith
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ssm.api.extentions.toDataSsm
import ssm.couchdb.dsl.query.CouchdbSsmListQuery
import ssm.couchdb.dsl.query.CouchdbSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmListQuery
import ssm.data.dsl.features.query.DataSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmListQueryResult
import ssm.data.dsl.model.DataSsm

class DataSsmListQueryFunctionImp(
    private val couchdbSsmListQueryFunction: CouchdbSsmListQueryFunction
): DataSsmListQueryFunction {

    private fun queryEachChannel(query: CouchdbSsmListQuery, chaincode: ChaincodeUri): Flow<DataSsm> = flow {
        emitAll(
            query.invokeWith(couchdbSsmListQueryFunction)
                .items
                .asFlow()
                .map { ssm -> ssm.toDataSsm(chaincode) }
        )
    }

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(msgs: Flow<DataSsmListQuery>): Flow<DataSsmListQueryResult> =
        msgs.map { payload ->
            payload.chaincodes
                .asFlow()
                .flatMapConcat { chaincodeUri ->
                    val query = CouchdbSsmListQuery(
                        chaincodeId = chaincodeUri.chaincodeId,
                        channelId = chaincodeUri.channelId,
                        pagination = null
                    )
                    queryEachChannel(query = query, chaincode = chaincodeUri)
                }
                .toList()
                .run(::DataSsmListQueryResult)
        }
}

