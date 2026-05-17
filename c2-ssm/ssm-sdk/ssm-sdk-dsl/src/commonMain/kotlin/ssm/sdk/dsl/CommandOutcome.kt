package ssm.sdk.dsl

/**
 * SDK-side mirror of the gateway InvokeOutcome wire shape. KMP common so
 * that both JVM and JS consumers (plateform backend + plateform-web) can
 * use the same type without a separate codegen step.
 */
data class CommandOutcome(
    val outcome: String, // "Committed" | "Rejected" | "Transient" | "Indeterminate" | "Conflict"
    val commandId: String,
    val transactionId: String? = null,
    val blockNumber: Long? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val payload: String? = null,
    val errorClass: String = "UNKNOWN",
    val errorOrigin: String = "UNKNOWN",
)

/** Quick predicates used by retry policy code in plateform. */
val CommandOutcome.isSuccess: Boolean get() = outcome == "Committed"
val CommandOutcome.isRetryable: Boolean get() = outcome == "Transient" || outcome == "Conflict"
val CommandOutcome.isPermanent: Boolean get() = outcome == "Rejected"
val CommandOutcome.needsStateCheck: Boolean get() = outcome == "Indeterminate"
