package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ssm.chaincode.dsl.query.SsmGetSessionQuery
import ssm.chaincode.dsl.query.SsmGetSessionQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionResult
import ssm.sdk.core.GetSessionQuery
import ssm.sdk.core.SsmQueryService

class SsmGetSessionQueryFunctionImpl(
    private val queryService: SsmQueryService
) : SsmGetSessionQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309")
    override suspend fun invoke(msgs: Flow<SsmGetSessionQuery>): Flow<SsmGetSessionResult> {
        return msgs.map { payload ->
            GetSessionQuery(payload.chaincodeUri, payload.sessionName)
        }.let { queries ->
            val getSessionQueries = queries.toList()
            queryService.getSessions(getSessionQueries)
        }.map { session ->
            SsmGetSessionResult(
                item = session,
            )
        }.asFlow()
    }
}
