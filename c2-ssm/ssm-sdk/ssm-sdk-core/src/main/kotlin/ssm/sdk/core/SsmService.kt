package ssm.sdk.core

import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.sign.SsmCmdSigner

class SsmService(
	private val ssmRequester: SsmRequester,
	private val ssmCmdSigner: SsmCmdSigner
) {

	suspend fun signAndSend(build: () -> SsmCmd): InvokeReturn {
		return build().let { ssmCmd ->
			sign(ssmCmd)
		}.let { signed ->
			send(signed)
		}
	}

	suspend fun signsAndSend(
		build: () -> List<SsmCmd>
	): List<InvokeReturn> {
		return build().map { ssmCmd ->
			sign(ssmCmd)
		}.let { signed ->
			send(signed)
		}
	}

	suspend fun signssAndSend(
		build: () -> List<SsmCmd>
	): List<InvokeReturn> {
		return build().map { ssmCmd ->
			sign(ssmCmd)
		}.let { signed ->
			send(signed)
		}
	}

	fun signs(build: () -> List<SsmCmd>): List<SsmCmdSigned> {
		return build().map { ssmCmd -> sign(ssmCmd) }
	}

	fun signss(build: () -> List<SsmCmd>): List<SsmCmdSigned> {
		return build().map { ssmCmd -> sign(ssmCmd) }
	}

	fun sign(command: SsmCmd): SsmCmdSigned {
		return ssmCmdSigner.sign(command)
	}

	suspend fun send(ssmCommandSigned: SsmCmdSigned): InvokeReturn {
		return ssmRequester.invoke(ssmCommandSigned)
	}

	suspend fun send(ssmCommandSigneds: List<SsmCmdSigned>): List<InvokeReturn> {
		return ssmRequester.invoke(ssmCommandSigneds)
	}

	/**
	 * Signs each [SsmCmd] individually (per-item resilience) and sends all successfully-signed
	 * commands to the blockchain as a batch. Commands that fail to sign produce a
	 * [CommandOutcome] with outcome="Rejected" and errorCode="SIGN_FAILED" instead of
	 * aborting the whole batch. Closes leak O.
	 */
	@Suppress("TooGenericExceptionCaught")
	suspend fun invokeAllV2(
		cmds: List<SsmCmd>,
		commandIds: List<String>,
	): List<CommandOutcome> {
		require(commandIds.size == cmds.size) {
			"commandIds.size=${commandIds.size} must match cmds.size=${cmds.size}"
		}

		data class SignResult(
			val commandId: String,
			val signed: SsmCmdSigned?,
			val failure: CommandOutcome?,
		)

		val signResults = cmds.zip(commandIds).map { (cmd, commandId) ->
			runCatching { ssmCmdSigner.sign(cmd) }.fold(
				onSuccess = { signed ->
					SignResult(commandId = commandId, signed = signed, failure = null)
				},
				onFailure = { e ->
					SignResult(
						commandId = commandId,
						signed = null,
						failure = CommandOutcome(
							outcome = "Rejected",
							commandId = commandId,
							errorCode = "SIGN_FAILED",
							errorMessage = e.message,
						),
					)
				},
			)
		}

		val signFailures = signResults.mapNotNull { it.failure }
		val successSigned = signResults.mapNotNull { it.signed }
		val successCommandIds = signResults.filter { it.signed != null }.map { it.commandId }

		val invokeOutcomes = if (successSigned.isEmpty()) {
			emptyList()
		} else {
			ssmRequester.invokeAllV2(successSigned, successCommandIds)
		}

		return signFailures + invokeOutcomes
	}
}
