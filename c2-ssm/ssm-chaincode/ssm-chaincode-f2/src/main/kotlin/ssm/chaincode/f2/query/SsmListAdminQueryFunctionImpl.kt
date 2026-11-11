package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmListAdminQuery
import ssm.chaincode.dsl.query.SsmListAdminQueryFunction
import ssm.chaincode.dsl.query.SsmListAdminResult
import ssm.sdk.core.SsmQueryService

class SsmListAdminQueryFunctionImpl(
    private val queryService: SsmQueryService
) : SsmListAdminQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(msgs: Flow<SsmListAdminQuery>): Flow<SsmListAdminResult> = msgs.map { payload ->
        queryService.listAdmins(payload.chaincodeUri).let { items ->
            SsmListAdminResult(items.toTypedArray())
        }
    }
}
