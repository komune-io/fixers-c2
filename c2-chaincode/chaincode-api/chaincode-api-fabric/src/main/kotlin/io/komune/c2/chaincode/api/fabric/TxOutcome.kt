package io.komune.c2.chaincode.api.fabric

import org.hyperledger.fabric.protos.peer.TxValidationCode

/**
 * Per-transaction outcome returned by FabricGatewayClient.invoke. Replaces
 * the previous "throw on failure" semantics so partial batches return
 * structured per-item information instead of cancelling siblings.
 */
sealed interface TxOutcome {
    val commandId: String

    data class Committed(
        override val commandId: String,
        val transactionId: String,
        val blockNumber: Long,
        val payload: String,
    ) : TxOutcome

    data class Rejected(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
    ) : TxOutcome

    data class Transient(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
    ) : TxOutcome

    data class Indeterminate(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
    ) : TxOutcome

    data class Conflict(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
        val transactionId: String? = null,
        val blockNumber: Long? = null,
    ) : TxOutcome
}

object TxValidationCodeMapper {
    fun toOutcome(
        commandId: String,
        transactionId: String,
        blockNumber: Long,
        code: TxValidationCode,
    ): TxOutcome = when (code) {
        TxValidationCode.VALID -> TxOutcome.Committed(commandId, transactionId, blockNumber, payload = "")
        TxValidationCode.MVCC_READ_CONFLICT,
        TxValidationCode.PHANTOM_READ_CONFLICT,
        TxValidationCode.ENDORSEMENT_POLICY_FAILURE,
        TxValidationCode.INVALID_OTHER_REASON,
        -> TxOutcome.Conflict(commandId, code.name, "validation failed: ${code.name}", transactionId, blockNumber)
        else -> TxOutcome.Rejected(commandId, code.name, "validation failed: ${code.name}")
    }
}
