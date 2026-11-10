package ssm.api.features.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.api.extentions.getSessionLogs
import ssm.api.extentions.getTransaction
import ssm.chaincode.dsl.model.uri.asChaincodeUri
import ssm.chaincode.dsl.model.uri.burst
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryDTO
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryResult
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryResultDTO
import ssm.data.dsl.model.DataSsmSessionState

class DataSsmSessionLogListQueryFunctionImpl(
    private val ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
    private val ssmGetTransactionQueryFunction: SsmGetTransactionQueryFunction,
) : DataSsmSessionLogListQueryFunction {

    override suspend fun invoke(msgs: Flow<DataSsmSessionLogListQueryDTO>): Flow<DataSsmSessionLogListQueryResultDTO> =
        msgs.map { payload ->
            val logs = payload.sessionName.getSessionLogs(payload.ssmUri.burst(), ssmGetSessionLogsQueryFunction)
            logs.map { log ->
                val transaction = log.txId.getTransaction(
                    ssmGetTransactionQueryFunction,
                    chaincodeUri = payload.ssmUri.asChaincodeUri(),
                )
                DataSsmSessionState(
                    details = log.state,
                    transaction = transaction
                )
            }.let {
                DataSsmSessionLogListQueryResult(it)
            }
        }
}
