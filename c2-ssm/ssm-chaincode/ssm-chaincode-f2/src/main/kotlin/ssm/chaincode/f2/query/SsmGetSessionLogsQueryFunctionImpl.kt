package ssm.chaincode.f2.query

import f2.dsl.fnc.operators.batch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.toBatch
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.sdk.core.GetLogQuery
import ssm.sdk.core.SsmQueryService

class SsmGetSessionLogsQueryFunctionImpl(
    private val batch: SsmBatchProperties,
    private val queryService: SsmQueryService,
): SsmGetSessionLogsQueryFunction  {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(
        msgs: Flow<SsmGetSessionLogsQuery>
    ): Flow<SsmGetSessionLogsQueryResult> = msgs.map { payload ->
        GetLogQueryWithSsmName(
            ssmName = payload.ssmName,
            getLogQuery = GetLogQuery(
                chaincodeUri = payload.chaincodeUri,
                sessionName = payload.sessionName
            )
        )
    }.batch(batch.toBatch()) { queries ->
        val logsResult = queryService.getLogs(queries.map { it.getLogQuery })
        val logBySessionName = logsResult.associateBy { log ->
            log.firstOrNull()?.run { state.session }
        }
        queries.map { query ->
            val logs = logBySessionName[query.getLogQuery.sessionName].orEmpty()
            SsmGetSessionLogsQueryResult(
                ssmName = query.ssmName,
                sessionName = query.getLogQuery.sessionName,
                logs = logs
            )
        }
    }
}

data class GetLogQueryWithSsmName(
    val ssmName: SsmName,
    val getLogQuery: GetLogQuery,
)
