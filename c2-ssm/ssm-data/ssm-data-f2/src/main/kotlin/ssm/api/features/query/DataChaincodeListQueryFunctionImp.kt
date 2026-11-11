package ssm.api.features.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.burst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.couchdb.dsl.query.CouchdbChaincodeListQuery
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryFunction
import ssm.data.dsl.features.query.DataChaincodeListQuery
import ssm.data.dsl.features.query.DataChaincodeListQueryFunction
import ssm.data.dsl.features.query.DataChaincodeListQueryResult

class DataChaincodeListQueryFunctionImp(
    private val couchdbChaincodeListQueryFunction: CouchdbChaincodeListQueryFunction
): DataChaincodeListQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309")
    override suspend fun invoke(msgs: Flow<DataChaincodeListQuery>): Flow<DataChaincodeListQueryResult> =
        msgs.map {
            couchdbChaincodeListQueryFunction.invoke(CouchdbChaincodeListQuery()).let { result ->
                DataChaincodeListQueryResult(result.items.map { chaincodeUri -> chaincodeUri.burst() })
            }
        }
}
