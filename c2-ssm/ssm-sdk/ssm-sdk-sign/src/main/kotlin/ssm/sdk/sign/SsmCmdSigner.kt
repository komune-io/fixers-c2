package ssm.sdk.sign

import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdSigned

fun interface SsmCmdSigner {
	fun sign(ssmCommand: SsmCmd): SsmCmdSigned
}
