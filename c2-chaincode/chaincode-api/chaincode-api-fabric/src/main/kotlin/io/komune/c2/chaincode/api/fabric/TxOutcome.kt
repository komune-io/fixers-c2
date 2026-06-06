package io.komune.c2.chaincode.api.fabric

import org.hyperledger.fabric.protos.peer.TxValidationCode
import s2.dsl.automate.ErrorCategory

/**
 * Per-transaction outcome returned by FabricGatewayClient.invoke. Replaces
 * the previous "throw on failure" semantics so partial batches return
 * structured per-item information instead of cancelling siblings.
 *
 * Shape-aligned with [PersistOutcome] / [LoadOutcome] in s2-automate-core:
 * a [Failure] parent groups the four error variants and pins each one to
 * the shared [ErrorCategory] taxonomy so downstream consumers key their
 * retry policy off `category` without pattern-matching every subtype.
 * Unlike PersistOutcome/LoadOutcome, failures carry stringly-typed
 * `errorCode` / `errorMessage` rather than an `S2Error` — the Fabric layer
 * has no concept of structured S2 errors.
 */
sealed interface TxOutcome {
    val msgId: String

    sealed interface Failure : TxOutcome {
        val errorCode: String
        val errorMessage: String
        val category: ErrorCategory
    }

    data class Committed(
        override val msgId: String,
        val transactionId: String,
        val blockNumber: Long,
        val payload: String,
    ) : TxOutcome

    data class Rejected(
        override val msgId: String,
        override val errorCode: String,
        override val errorMessage: String,
    ) : Failure {
        override val category: ErrorCategory = ErrorCategory.Rejected
    }

    data class Transient(
        override val msgId: String,
        override val errorCode: String,
        override val errorMessage: String,
    ) : Failure {
        override val category: ErrorCategory = ErrorCategory.Transient
    }

    data class Indeterminate(
        override val msgId: String,
        override val errorCode: String,
        override val errorMessage: String,
    ) : Failure {
        override val category: ErrorCategory = ErrorCategory.Indeterminate
    }

    data class Conflict(
        override val msgId: String,
        override val errorCode: String,
        override val errorMessage: String,
        val transactionId: String? = null,
        val blockNumber: Long? = null,
    ) : Failure {
        override val category: ErrorCategory = ErrorCategory.Conflict
    }
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
        msgId: String,
        statusCodeName: String,
        transactionId: String?,
        blockNumber: Long?,
    ): TxOutcome = when (statusCodeName) {
        "MVCC_READ_CONFLICT", "PHANTOM_READ_CONFLICT", "INVALID_OTHER_REASON",
        "DUPLICATE_TXID", "INVALID_WRITESET" ->
            TxOutcome.Conflict(
                msgId = msgId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
                transactionId = transactionId,
                blockNumber = blockNumber,
            )
        "ENDORSEMENT_POLICY_FAILURE" ->
            TxOutcome.Rejected(
                msgId = msgId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
            )
        "UNAUTHORISED" ->
            TxOutcome.Rejected(
                msgId = msgId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
            )
        "TARGET_CHAIN_NOT_FOUND" ->
            TxOutcome.Rejected(
                msgId = msgId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
            )
        "BAD_RWSET", "BAD_CHANNEL_HEADER",
        "BAD_HEADER_EXTENSION", "INVALID_CONFIG_TRANSACTION", "MARSHAL_TX_ERROR",
        "NIL_ENVELOPE", "BAD_PAYLOAD", "BAD_COMMON_HEADER", "BAD_CREATOR_SIGNATURE",
        "INVALID_ENDORSER_TRANSACTION", "UNSUPPORTED_TX_PAYLOAD", "BAD_PROPOSAL_TXID",
        "UNKNOWN_TX_TYPE", "NIL_TXACTION", "EXPIRED_CHAINCODE",
        "CHAINCODE_VERSION_CONFLICT", "BAD_RESPONSE_PAYLOAD", "ILLEGAL_WRITESET",
        "INVALID_CHAINCODE" ->
            TxOutcome.Rejected(
                msgId = msgId,
                errorCode = statusCodeName,
                errorMessage = "validation: $statusCodeName",
            )
        else ->
            TxOutcome.Indeterminate(
                msgId = msgId,
                errorCode = statusCodeName,
                errorMessage = "unknown validation code: $statusCodeName",
            )
    }

    /**
     * Enum-based overload retained for call sites that have the proto enum
     * available (e.g. TxValidationCodeMapperTest). Delegates to the string form.
     */
    fun toOutcome(
        msgId: String,
        transactionId: String,
        blockNumber: Long,
        code: TxValidationCode,
    ): TxOutcome = if (code == TxValidationCode.VALID) {
        TxOutcome.Committed(msgId, transactionId, blockNumber, payload = "")
    } else {
        toOutcome(
            msgId = msgId,
            statusCodeName = code.name,
            transactionId = transactionId,
            blockNumber = blockNumber,
        )
    }
}
