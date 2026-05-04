package ssm.chaincode.dsl.model

import io.komune.c2.chaincode.dsl.TransactionId
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

@JsExport
@JsName("SsmSessionStateLogDTO")
interface SsmSessionStateLogDTO {
	/**
	 * Id of the [Transaction][ssm.chaincode.dsl.blockchain.Transaction] the state is originated from
	 * @example [ssm.chaincode.dsl.blockchain.Transaction.transactionId]
	 */
	val txId: TransactionId

	/**
	 * The state generated with the transaction
	 */
	val state: SsmSessionStateDTO
}

/**
 * Associates a session state with the actual blockchain transaction that lead to it
 * @d2 model
 * @parent [SsmSession]
 * @order 20
 */
@Serializable
@JsExport
@JsName("SsmSessionStateLog")
data class SsmSessionStateLog(
    override val txId: TransactionId,
    override val state: SsmSessionState,
) : SsmSessionStateLogDTO
