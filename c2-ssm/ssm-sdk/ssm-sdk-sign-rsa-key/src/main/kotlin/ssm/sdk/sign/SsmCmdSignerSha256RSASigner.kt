package ssm.sdk.sign

import java.util.Base64
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.sign.crypto.Sha256RSASigner
import ssm.sdk.sign.exception.SsmSignException
import ssm.sdk.sign.model.Signer

open class SsmCmdSignerSha256RSASigner(vararg signer: Signer): SsmCmdSigner {
	private val signers = signer.associateBy { it.name }
	override fun sign(ssmCommand: SsmCmd): SsmCmdSigned {
		val signer = signers[ssmCommand.agentName]
			?: throw SsmSignException("Invalid agent name: ${ssmCommand.agentName}")
		val signature = Sha256RSASigner.rsaSign(ssmCommand.valueToSign, signer.pair.private)
		return SsmCmdSigned(
			cmd = ssmCommand,
			signer = signer.name,
			signature = Base64.getEncoder().encodeToString(signature),
			chaincodeUri = ssmCommand.chaincodeUri
		)
	}
}
