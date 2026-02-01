package ssm.chaincode.dsl.blockchain

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

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
	 * Execution date of the transaction as epoch milliseconds
	 * @example 1738410430231
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
	 * Random value used to prevent replay attacks.
	 * Each transaction includes a unique nonce to ensure it cannot be submitted twice.
	 * @example "j/gVtg9VQ+8tvui/GTyxlILV8HIakce3"
	 */
	val nonce: ByteArray

	/**
	 * Type of the transaction envelope.
	 * @example [EnvelopeType.TRANSACTION_ENVELOPE]
	 */
	val type: EnvelopeType

	/**
	 * Validation status code returned by the peer.
	 * 0 indicates a valid transaction, other values indicate validation errors.
	 * @example 0
	 */
	val validationCode: Byte
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
	override val type: EnvelopeType,
	override val validationCode: Byte,
) : TransactionDTO

typealias TransactionId = String
