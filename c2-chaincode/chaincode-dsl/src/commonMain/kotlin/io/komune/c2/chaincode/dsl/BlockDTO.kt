package io.komune.c2.chaincode.dsl

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

typealias BlockId = Int

@JsExport
@JsName("BlockDTO")
interface BlockDTO {
	/**
	 * Identifier of the block
	 * @example 10
	 */
	val blockId: BlockId

	/**
	 * Hash of the previous block within the blockchain
	 * @example "TODO"
	 */
	val previousHash: ByteArray

	/**
	 * Hash of the data within the block
	 * @example "TODO"
	 */
	val dataHash: ByteArray

	/**
	 * Transactions within the block
	 */
	val transactions: List<TransactionDTO>
}

/**
 * Block of transactions stored within the blockchain
 * @d2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 */
@Serializable
@JsExport
@JsName("Block")
class Block(
    override val blockId: BlockId,
    override val previousHash: ByteArray,
    override val dataHash: ByteArray,
    override val transactions: List<Transaction>,
) : BlockDTO
