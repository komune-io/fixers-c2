package ssm.api.features.query.internal

import f2.dsl.fnc.F2Function
import f2.dsl.fnc.operators.flattenConcurrently
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.Transaction
import io.komune.c2.chaincode.dsl.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.asChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.chaincode.dsl.query.SsmGetTransactionQuery
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.data.dsl.model.DataChannel
import ssm.data.dsl.model.DataSsmSession
import ssm.data.dsl.model.DataSsmSessionState

class DataSsmSessionConvertFunctionImpl(
	private val ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
	private val ssmGetTransactionQueryFunction: SsmGetTransactionQueryFunction
) : F2Function<DataSsmSessionConvertQuery, DataSsmSession> {

	override suspend fun invoke(msgs: Flow<DataSsmSessionConvertQuery>): Flow<DataSsmSession> {
		val allMsgs = msgs.toList()
		val allSessionState = allMsgs.map { it.sessionState }.associateBy { it.session }

		return allMsgs.groupBy { it.ssmUri }.map { (ssmUri, flow) ->
			val allSessionLogs =
				flow.map { it.sessionState.session }.getSessionLogs(ssmUri, ssmGetSessionLogsQueryFunction)
			allSessionLogs.map { sessionLogs ->
				val transactions = sessionLogs.logs.map { it.txId }.getTransactions(ssmUri.asChaincodeUri())
				val state = allSessionState[sessionLogs.sessionName]!!
				sessionLogs.toDataSession(ssmUri, state, transactions)
			}
		}.asFlow().flattenConcurrently()
	}

	private fun SsmGetSessionLogsQueryResult.toDataSession(
		ssmUri: SsmUri,
		state: SsmSessionState,
		transactions: List<Transaction>
	): DataSsmSession {
		val firstTransaction = transactions.minByOrNull { transaction ->
			transaction.timestamp
		}
		val lastTransaction = transactions.maxByOrNull { transaction ->
			transaction.timestamp
		}

		return DataSsmSession(
			ssmUri = ssmUri,
			sessionName = this.sessionName,
			state = DataSsmSessionState(
				details = state,
				transaction = lastTransaction
			),
			channel = DataChannel(ssmUri.channelId),
			transaction = firstTransaction,
			transactions = transactions
		)
	}


	suspend fun List<TransactionId>.getTransactions(
		chaincodeUri: ChaincodeUri
	): List<Transaction> {
		val queries = this.map { transactionId ->
			SsmGetTransactionQuery(
				chaincodeUri = chaincodeUri,
				id = transactionId,
			)
		}
		return ssmGetTransactionQueryFunction.invoke(queries.asFlow()).mapNotNull { it.item }.toList()
	}

	suspend fun List<SessionName>.getSessionLogs(
		ssmUri: SsmUri,
		ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
	): Flow<SsmGetSessionLogsQueryResult> = map { sessionName ->
		SsmGetSessionLogsQuery(
			chaincodeUri = ssmUri.chaincodeUri,
			sessionName = sessionName,
			ssmName = ssmUri.ssmName
		)
	}.let {
		ssmGetSessionLogsQueryFunction.invoke(it.asFlow())
	}.map { it }


}

data class DataSsmSessionConvertQuery(
	val ssmUri: SsmUri,
	val sessionState: SsmSessionState
)
