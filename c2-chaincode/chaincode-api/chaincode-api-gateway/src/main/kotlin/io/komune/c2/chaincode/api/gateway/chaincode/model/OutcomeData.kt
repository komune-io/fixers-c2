package io.komune.c2.chaincode.api.gateway.chaincode.model

/**
 * CloudEvents `data` payload for `/invoke` responses.
 *
 * Single shape covering all 5 outcome variants — the envelope `type`
 * attribute (see `InvokeType.Outcome.*`) disambiguates which fields are populated.
 *
 * - Committed: transactionId, blockNumber, payload
 * - Rejected / Transient / Indeterminate: errorCode, errorMessage
 * - Conflict: transactionId, blockNumber, errorCode, errorMessage
 */
data class OutcomeData(
    val transactionId: String? = null,
    val blockNumber: Long? = null,
    val payload: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)
