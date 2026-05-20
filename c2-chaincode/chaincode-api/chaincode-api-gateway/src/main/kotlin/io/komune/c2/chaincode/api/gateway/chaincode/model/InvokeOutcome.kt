package io.komune.c2.chaincode.api.gateway.chaincode.model

/**
 * Wire DTO returned by POST /invoke/v2. Mirrors fabric.TxOutcome but
 * flattens the sealed hierarchy into a tagged record because Spring
 * WebFlux + Jackson polymorphism would otherwise require type
 * annotations across the wire boundary.
 */
data class InvokeOutcome(
    val outcome: String, // "Committed" | "Rejected" | "Transient" | "Indeterminate" | "Conflict"
    val msgId: String,
    val transactionId: String? = null,
    val blockNumber: Long? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val payload: String? = null,
)
