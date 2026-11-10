package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmGetQuery
import ssm.chaincode.dsl.query.SsmGetQueryFunction
import ssm.chaincode.dsl.query.SsmGetResult
import ssm.sdk.core.SsmQueryService

class SsmGetQueryFunctionImpl(
    private val queryService: SsmQueryService
) : SsmGetQueryFunction {

    override suspend fun invoke(msgs: Flow<SsmGetQuery>): Flow<SsmGetResult> = msgs.map { payload ->
        queryService.getSsm(payload.chaincodeUri, payload.name).let(::SsmGetResult)
    }
}
