package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmGetAdminFunction
import ssm.chaincode.dsl.query.SsmGetAdminQuery
import ssm.chaincode.dsl.query.SsmGetAdminResult
import ssm.sdk.core.SsmQueryService

class SsmGetAdminFunctionImpl(private val queryService: SsmQueryService) : SsmGetAdminFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309")
    override suspend fun invoke(msgs: Flow<SsmGetAdminQuery>): Flow<SsmGetAdminResult> = msgs.map { payload ->
        val sessionState = queryService.getAdmin(payload.chaincodeUri, payload.name)
        SsmGetAdminResult(sessionState)
    }
}
