package ssm.sdk.sign.model

import java.security.KeyPair
import ssm.chaincode.dsl.model.AgentName

interface Signer {
    val name: AgentName
    val pair: KeyPair
}
