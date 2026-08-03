package ssm.api.features.query

import f2.dsl.fnc.invoke
import io.komune.c2.chaincode.dsl.IdentitiesInfo
import io.komune.c2.chaincode.dsl.Transaction
import kotlinx.coroutines.flow.map
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.chaincode.dsl.query.SsmGetTransactionQueryResult
import ssm.data.dsl.features.query.DataSsmSessionLogGetQuery

class DataSsmSessionLogGetQueryFunctionImplUnitTest {

	private val ssmUri = SsmUri("ssm:sandbox:ssm:CarDealership")

	private fun log(txId: String, iteration: Int) = SsmSessionStateLog(
		txId = txId,
		state = SsmSessionState(
			ssm = "CarDealership",
			session = "deal-1",
			roles = mapOf("admin" to "Seller"),
			public = "data",
			private = emptyMap(),
			origin = SsmTransition(from = 0, to = 1, role = "Seller", action = "Sell"),
			current = 1,
			iteration = iteration,
		)
	)

	private fun transaction(txId: String) = Transaction(
		transactionId = txId,
		blockId = 7,
		timestamp = 1_700_000_000_000,
		isValid = true,
		channelId = "sandbox",
		creator = IdentitiesInfo(mspid = "msp", id = "identity"),
		nonce = "nonce".toByteArray(),
	)

	private fun logsFunction(logs: List<SsmSessionStateLog>) = SsmGetSessionLogsQueryFunction { msgs ->
		msgs.map { query ->
			SsmGetSessionLogsQueryResult(
				ssmName = query.ssmName,
				sessionName = query.sessionName,
				logs = logs,
			)
		}
	}

	private fun transactionFunction() = SsmGetTransactionQueryFunction { msgs ->
		msgs.map { query -> SsmGetTransactionQueryResult(transaction(query.id)) }
	}

	@Test
	suspend fun `returns the session state matching the transaction id`() {
		val function = DataSsmSessionLogGetQueryFunctionImpl(
			logsFunction(listOf(log("tx-1", 0), log("tx-2", 1))),
			transactionFunction(),
		)

		val result = function.invoke(
			DataSsmSessionLogGetQuery(sessionName = "deal-1", txId = "tx-2", ssmUri = ssmUri)
		)

		assertThat(result.item?.details?.iteration).isEqualTo(1)
		assertThat(result.item?.transaction?.transactionId).isEqualTo("tx-2")
	}

	@Test
	suspend fun `returns a null item when no log matches the transaction id`() {
		val function = DataSsmSessionLogGetQueryFunctionImpl(
			logsFunction(listOf(log("tx-1", 0))),
			transactionFunction(),
		)

		val result = function.invoke(
			DataSsmSessionLogGetQuery(sessionName = "deal-1", txId = "tx-9", ssmUri = ssmUri)
		)

		assertThat(result.item).isNull()
	}
}
