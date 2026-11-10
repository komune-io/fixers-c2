package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmListSsmQuery
import ssm.chaincode.dsl.query.SsmListSsmQueryFunction
import ssm.chaincode.dsl.query.SsmListSsmResult
import ssm.sdk.core.SsmQueryService

class SsmListSsmQueryFunctionImpl(
    private val queryService: SsmQueryService
): SsmListSsmQueryFunction {

    override suspend fun invoke(msgs: Flow<SsmListSsmQuery>): Flow<SsmListSsmResult> = msgs.map { payload ->
        queryService.listSsm(payload.chaincodeUri).let { items ->
            SsmListSsmResult(items.toTypedArray())
        }
    }
}
