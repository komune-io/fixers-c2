package ssm.api.features.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.api.extentions.getSessionLogs
import ssm.api.extentions.getTransaction
import ssm.chaincode.dsl.model.uri.asChaincodeUri
import ssm.chaincode.dsl.model.uri.burst
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogGetQueryDTO
import ssm.data.dsl.features.query.DataSsmSessionLogGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogGetQueryResult
import ssm.data.dsl.features.query.DataSsmSessionLogGetQueryResultDTO
import ssm.data.dsl.model.DataSsmSessionState

class DataSsmSessionLogGetQueryFunctionImpl(
    private val ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
    private val ssmGetTransactionQueryFunction: SsmGetTransactionQueryFunction
) : DataSsmSessionLogGetQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309")
    override suspend fun invoke(msgs: Flow<DataSsmSessionLogGetQueryDTO>): Flow<DataSsmSessionLogGetQueryResultDTO> =
        msgs.map { payload ->
            val logs = payload.sessionName.getSessionLogs(
                payload.ssmUri.burst(),
                ssmGetSessionLogsQueryFunction
            )
            val transaction = payload.txId.getTransaction(
                ssmGetTransactionQueryFunction,
                chaincodeUri = payload.ssmUri.asChaincodeUri(),
            )
            logs.firstOrNull { log -> log.txId == payload.txId }
                ?.let { sessionState ->
                    DataSsmSessionState(
                        details = sessionState.state,
                        transaction = transaction
                    )
                }
                .run(::DataSsmSessionLogGetQueryResult)
        }
}
