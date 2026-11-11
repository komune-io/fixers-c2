package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmListSessionQuery
import ssm.chaincode.dsl.query.SsmListSessionQueryFunction
import ssm.chaincode.dsl.query.SsmListSessionResult
import ssm.sdk.core.SsmQueryService

class SsmListSessionQueryFunctionImpl(
    private val queryService: SsmQueryService
) : SsmListSessionQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309")
    override suspend fun invoke(msgs: Flow<SsmListSessionQuery>): Flow<SsmListSessionResult> = msgs.map { payload ->
        val list = queryService.listSession(payload.chaincodeUri)
        SsmListSessionResult(list.toTypedArray())
    }
}
