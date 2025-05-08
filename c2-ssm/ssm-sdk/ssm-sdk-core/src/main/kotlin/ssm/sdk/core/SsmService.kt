package ssm.sdk.core

import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import ssm.sdk.core.ktor.SsmRequester
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

	fun sign(command: SsmCmd): SsmCmdSigned {
		return ssmCmdSigner.sign(command)
	}

	suspend fun send(ssmCommandSigned: SsmCmdSigned): InvokeReturn {
		return ssmRequester.invoke(ssmCommandSigned)
	}

	suspend fun send(ssmCommandSigneds: List<SsmCmdSigned>): List<InvokeReturn> {
		return ssmRequester.invoke(ssmCommandSigneds)
	}
}
