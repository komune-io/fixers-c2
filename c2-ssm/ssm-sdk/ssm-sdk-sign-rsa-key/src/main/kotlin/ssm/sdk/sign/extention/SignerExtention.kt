package ssm.sdk.sign.extention

import ssm.chaincode.dsl.model.Agent
import ssm.sdk.sign.model.Signer

fun Signer.asAgent(): Agent {
    return Agent(
        this.name,
        this.pair.public.encoded
    )
}
