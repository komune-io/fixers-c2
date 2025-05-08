package ssm.api.extentions

import f2.dsl.fnc.invokeWith
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.Transaction
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmDTO
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.model.uri.DEFAULT_VERSION
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetTransactionQuery
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.data.dsl.model.DataChannel
import ssm.data.dsl.model.DataSsm

fun SsmDTO.toDataSsm(burst: ChaincodeUri): DataSsm {
	return DataSsm(
		ssm = Ssm(
			name = this.name,
			transitions = this.transitions.map { transition ->
				SsmTransition(
					from = transition.from,
					to = transition.to,
					role = transition.role,
					action = transition.action
				)
			}
		),
		version = DEFAULT_VERSION,
		channel = DataChannel(burst.channelId),
		uri = burst.toSsmUri(this.name)
	)
}

fun Ssm.toDataSsm(ssm: SsmUri): DataSsm {
	return DataSsm(
		ssm = this,
		version = ssm.ssmVersion,
		channel = DataChannel(ssm.channelId),
		uri = ssm
	)
}

suspend fun SessionName.getSessionLogs(
	ssmUri: SsmUri,
	ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
): List<SsmSessionStateLog> {
	return try {
		SsmGetSessionLogsQuery(
			chaincodeUri = ssmUri.chaincodeUri,
			sessionName = this,
			ssmName = ssmUri.ssmName
		).invokeWith(ssmGetSessionLogsQueryFunction).logs
	} catch (e: Exception) {
		emptyList()
	}
}

suspend fun TransactionId?.getTransaction(
	ssmGetTransactionQueryFunction: SsmGetTransactionQueryFunction,
	chaincodeUri: ChaincodeUri
): Transaction? {
	return this?.let { transactionId ->
		SsmGetTransactionQuery(
			chaincodeUri = chaincodeUri,
			id = transactionId,
		).invokeWith(ssmGetTransactionQueryFunction).item
	}
}
