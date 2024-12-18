package io.komune.c2.chaincode.api.gateway.chaincode.model

import io.komune.c2.chaincode.api.fabric.model.InvokeArgs
import io.komune.c2.chaincode.api.gateway.config.ChainCodeId
import io.komune.c2.chaincode.api.gateway.config.ChannelId

data class InvokeParams(
    val channelid: ChannelId? = null,
    val chaincodeid: ChainCodeId? = null,
    val cmd: Cmd,
    val fcn: String,
    val args: Array<String>
)


fun List<InvokeParams>.toInvokeArgs(): List<InvokeArgs> = map {
    it.toInvokeArgs()
}

fun InvokeParams.toInvokeArgs(): InvokeArgs {
    return InvokeArgs(fcn, args.iterator())
}
