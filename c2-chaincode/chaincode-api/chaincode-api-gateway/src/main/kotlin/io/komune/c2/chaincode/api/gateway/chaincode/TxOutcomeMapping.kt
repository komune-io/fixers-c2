package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.api.gateway.chaincode.model.OutcomeData
import io.komune.c2.chaincode.dsl.cloudevent.InvokeType

/**
 * Maps a [TxOutcome] sealed variant to its CloudEvent type-string + [OutcomeData] payload.
 *
 * Single source of truth for the wire mapping — used by [ChaincodeService] in production
 * and reused by tests that script `TxOutcome` values to exercise the end-to-end CE shape.
 */
internal fun TxOutcome.toWire(): Pair<String, OutcomeData> = when (this) {
	is TxOutcome.Committed -> InvokeType.Outcome.COMMITTED to OutcomeData(
		transactionId = transactionId, blockNumber = blockNumber, payload = payload,
	)
	is TxOutcome.Rejected -> InvokeType.Outcome.REJECTED to OutcomeData(
		errorCode = errorCode, errorMessage = errorMessage,
	)
	is TxOutcome.Transient -> InvokeType.Outcome.TRANSIENT to OutcomeData(
		errorCode = errorCode, errorMessage = errorMessage,
	)
	is TxOutcome.Indeterminate -> InvokeType.Outcome.INDETERMINATE to OutcomeData(
		errorCode = errorCode, errorMessage = errorMessage,
	)
	is TxOutcome.Conflict -> InvokeType.Outcome.CONFLICT to OutcomeData(
		transactionId = transactionId, blockNumber = blockNumber,
		errorCode = errorCode, errorMessage = errorMessage,
	)
}
