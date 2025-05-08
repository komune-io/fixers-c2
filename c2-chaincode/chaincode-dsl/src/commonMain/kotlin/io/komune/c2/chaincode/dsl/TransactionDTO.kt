package io.komune.c2.chaincode.dsl

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

typealias TransactionId = String

@JsExport
@JsName("TransactionDTO")
interface TransactionDTO {
	/**
	 * Identifier of the transaction
	 * @example "c7de3ab6-56e0-4e7d-8fa4-905823ed982e"
	 */
	val transactionId: TransactionId

	/**
	 * [Block] holding the transaction within the blockchain
	 * @example [Block.blockId]
	 */
	val blockId: BlockId

	/**
	 * Execution date of the transaction
	 * @example 1627984925000
	 */
	val timestamp: Long

	/**
	 * Indicates if the transaction has been validated or not
	 * @example true
	 */
	val isValid: Boolean

	/**
	 * Channel in which the transaction has been performed
	 * @example "channel-komune"
	 */
	val channelId: String

	/**
	 * Requester of the transaction
	 */
	val creator: IdentitiesInfoDTO

	/**
	 * Nonce of the transaction
	 * @example "TODO"
	 */
	val nonce: ByteArray

	/**
	 * Validation code of the transaction
	 * @example "TODO"
	 */
//	val validationCode: Byte
}

/**
 * @d2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 * @title SSM-CHAINCODE/Blockchain Content
 */
@Serializable
@JsName("Transaction")
@JsExport
class Transaction(
	override val transactionId: TransactionId,
	override val blockId: BlockId,
	override val timestamp: Long,
	override val isValid: Boolean,
	override val channelId: String,
	override val creator: IdentitiesInfo,
	override val nonce: ByteArray,
//	override val validationCode: Byte,
) : TransactionDTO

