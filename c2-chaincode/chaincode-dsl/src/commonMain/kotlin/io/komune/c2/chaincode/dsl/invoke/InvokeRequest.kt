package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.InvokeFunction

data class InvokeRequest(
    val channelid: ChannelId? = null,
    val chaincodeid: ChaincodeId? = null,
    val cmd: InvokeRequestType,
    val fcn: String,
    val args: Array<String>
)

@Suppress("EnumNaming")
enum class InvokeRequestType {
    query, invoke
}


fun List<InvokeRequest>.toInvokeArgs(): List<InvokeArgs> = map {
    it.toInvokeArgs()
}

fun InvokeRequest.toInvokeArgs(): InvokeArgs {
    return InvokeArgs(InvokeFunction(fcn), args.toList())
}

