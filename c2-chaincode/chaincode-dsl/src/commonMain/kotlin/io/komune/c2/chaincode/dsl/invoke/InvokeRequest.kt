package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.InvokeFunction

data class InvokeRequest(
    val channelid: ChannelId? = null,
    val chaincodeid: ChaincodeId? = null,
    val cmd: InvokeRequestType,
    val fcn: String,
    val args: Array<String>,
    val timestamp: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InvokeRequest

        return channelid == other.channelid &&
            chaincodeid == other.chaincodeid &&
            cmd == other.cmd &&
            fcn == other.fcn &&
            args.contentEquals(other.args) &&
            timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = channelid?.hashCode() ?: 0
        result = 31 * result + (chaincodeid?.hashCode() ?: 0)
        result = 31 * result + cmd.hashCode()
        result = 31 * result + fcn.hashCode()
        result = 31 * result + args.contentHashCode()
        result = 31 * result + (timestamp?.hashCode() ?: 0)
        return result
    }
}

@Suppress("EnumNaming")
enum class InvokeRequestType {
    query, invoke
}


fun List<InvokeRequest>.toInvokeArgs(): List<InvokeArgs> = map {
    it.toInvokeArgs()
}

fun InvokeRequest.toInvokeArgs(): InvokeArgs {
    return InvokeArgs(InvokeFunction(fcn), args.toList(), timestamp = timestamp)
}

