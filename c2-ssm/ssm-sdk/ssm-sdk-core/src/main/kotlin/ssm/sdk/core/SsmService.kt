package ssm.sdk.core

import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.sign.SsmCmdSigner

class SsmService(
	private val ssmRequester: SsmRequester,
	private val ssmCmdSigner: SsmCmdSigner
) {

	fun signs(build: () -> List<SsmCmd>): List<SsmCmdSigned> {
		return build().map { ssmCmd -> sign(ssmCmd) }
	}

	fun signss(build: () -> List<SsmCmd>): List<SsmCmdSigned> {
		return build().map { ssmCmd -> sign(ssmCmd) }
	}

	fun sign(command: SsmCmd): SsmCmdSigned {
		return ssmCmdSigner.sign(command)
	}

	/**
	 * Signs each [SsmCmd] individually (per-item resilience) and sends all successfully-signed
	 * commands to the blockchain as a batch. Commands that fail to sign produce a
	 * [CommandOutcome] with outcome="Rejected" and errorCode="SIGN_FAILED" instead of
	 * aborting the whole batch.
	 *
	 * The returned list is NOT guaranteed to preserve the input command order;
	 * signing failures are emitted before invoke outcomes. Callers must key
	 * results by [CommandOutcome.msgId], never by position.
	 */
	suspend fun invokeAll(
		cmds: List<SsmCmd>,
		msgIds: List<String>,
	): List<CommandOutcome> {
		require(msgIds.size == cmds.size) {
			"msgIds.size=${msgIds.size} must match cmds.size=${cmds.size}"
		}

		data class SignResult(
			val msgId: String,
			val signed: SsmCmdSigned?,
			val failure: CommandOutcome?,
		)

		val signResults = cmds.zip(msgIds).map { (cmd, msgId) ->
			runCatching { ssmCmdSigner.sign(cmd) }.fold(
				onSuccess = { signed ->
					SignResult(msgId = msgId, signed = signed, failure = null)
				},
				onFailure = { e ->
					if (e is kotlinx.coroutines.CancellationException) throw e
					SignResult(
						msgId = msgId,
						signed = null,
						failure = CommandOutcome(
							outcome = "Rejected",
							msgId = msgId,
							errorCode = "SIGN_FAILED",
							errorMessage = e.message,
						),
					)
				},
			)
		}

		val signFailures = signResults.mapNotNull { it.failure }
		val successSigned = signResults.mapNotNull { it.signed }
		val successMsgIds = signResults.filter { it.signed != null }.map { it.msgId }

		val invokeOutcomes = if (successSigned.isEmpty()) {
			emptyList()
		} else {
			ssmRequester.invokeAll(successSigned, successMsgIds)
		}

		return signFailures + invokeOutcomes
	}
}
