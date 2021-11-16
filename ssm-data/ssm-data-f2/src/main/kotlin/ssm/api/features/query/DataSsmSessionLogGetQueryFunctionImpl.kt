package ssm.api.features.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.api.extentions.getSessionLogs
import ssm.api.extentions.getTransaction
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

	override suspend fun invoke(msg: Flow<DataSsmSessionLogGetQueryDTO>): Flow<DataSsmSessionLogGetQueryResultDTO> =
		msg.map { payload ->
			val logs = payload.sessionName.getSessionLogs(
				ssmGetSessionLogsQueryFunction
			)
			val transaction = payload.txId.getTransaction(
				ssmGetTransactionQueryFunction
			)
			logs.firstOrNull { log -> log.txId == payload.txId }
				?.let { sessionState ->
					DataSsmSessionState(
						details = sessionState.state,
						transaction = transaction
					)
				}.let {
					DataSsmSessionLogGetQueryResult(it)
				}
		}
}
