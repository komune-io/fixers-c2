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
        val errorClass: String = "UNKNOWN",
    ) : TxOutcome

    data class Transient(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
        val errorClass: String = "UNKNOWN",
    ) : TxOutcome

    data class Indeterminate(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
        val errorClass: String = "UNKNOWN",
    ) : TxOutcome

    data class Conflict(
        override val commandId: String,
        val errorCode: String,
        val errorMessage: String,
        val transactionId: String? = null,
        val blockNumber: Long? = null,
        val errorClass: String = "UNKNOWN",
    ) : TxOutcome
}

object TxValidationCodeMapper {

    /**
     * Maps a raw validation-code name (as returned by status.code.name at commit
     * time) to the appropriate TxOutcome subtype. Using the string form avoids a
     * hard dependency on the proto-generated TxValidationCode enum at call sites
     * that only have the name available.
     *
     * Conflict  — transient state issue; caller should refetch state and retry.
     * Rejected  — permanent policy / format failure; retrying will not help.
     * Indeterminate — unknown future code; operator investigation required.
     */
    fun toOutcome(
        commandId: String,
        statusCodeName: String,
        transactionId: String?,
        blockNumber: Long?,
    ): TxOutcome = when (statusCodeName) {
        "MVCC_READ_CONFLICT", "PHANTOM_READ_CONFLICT", "INVALID_OTHER_REASON",
        "DUPLICATE_TXID", "INVALID_WRITESET" ->
            TxOutcome.Conflict(
                commandId = commandId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
                transactionId = transactionId,
                blockNumber = blockNumber,
                errorClass = "STATE",
            )
        "ENDORSEMENT_POLICY_FAILURE" ->
            TxOutcome.Rejected(
                commandId = commandId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
                errorClass = "BUSINESS",
            )
        "UNAUTHORISED" ->
            TxOutcome.Rejected(
                commandId = commandId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
                errorClass = "AUTH",
            )
        "TARGET_CHAIN_NOT_FOUND" ->
            TxOutcome.Rejected(
                commandId = commandId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
                errorClass = "INFRA",
            )
        "BAD_RWSET", "BAD_CHANNEL_HEADER",
        "BAD_HEADER_EXTENSION", "INVALID_CONFIG_TRANSACTION", "MARSHAL_TX_ERROR",
        "NIL_ENVELOPE", "BAD_PAYLOAD", "BAD_COMMON_HEADER", "BAD_CREATOR_SIGNATURE",
        "INVALID_ENDORSER_TRANSACTION", "UNSUPPORTED_TX_PAYLOAD", "BAD_PROPOSAL_TXID",
        "UNKNOWN_TX_TYPE", "NIL_TXACTION", "EXPIRED_CHAINCODE",
        "CHAINCODE_VERSION_CONFLICT", "BAD_RESPONSE_PAYLOAD", "ILLEGAL_WRITESET",
        "INVALID_CHAINCODE" ->
            TxOutcome.Rejected(
                commandId = commandId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
                errorClass = "INPUT",
            )
        else ->
            TxOutcome.Indeterminate(
                commandId = commandId,
                errorCode = statusCodeName,
                errorMessage = "unknown validation code: $statusCodeName",
                errorClass = "UNKNOWN",
            )
    }

    /**
     * Enum-based overload retained for call sites that have the proto enum
     * available (e.g. TxValidationCodeMapperTest). Delegates to the string form.
     */
    fun toOutcome(
        commandId: String,
        transactionId: String,
        blockNumber: Long,
        code: TxValidationCode,
    ): TxOutcome = if (code == TxValidationCode.VALID) {
        TxOutcome.Committed(commandId, transactionId, blockNumber, payload = "")
    } else {
        toOutcome(
            commandId = commandId,
            statusCodeName = code.name,
            transactionId = transactionId,
            blockNumber = blockNumber,
        )
    }
}
