package io.komune.c2.chaincode.api.gateway.chaincode.model

import io.komune.c2.chaincode.api.gateway.config.ChainCodeId
import io.komune.c2.chaincode.api.gateway.config.ChannelId

data class InvokeParams(
    val channelid: ChannelId? = null,
    val chaincodeid: ChainCodeId? = null,
    val cmd: Cmd,
    val fcn: String,
    val args: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as InvokeParams
        return channelid == other.channelid &&
            chaincodeid == other.chaincodeid &&
            cmd == other.cmd &&
            fcn == other.fcn &&
            args.contentEquals(other.args)
    }

    override fun hashCode(): Int {
        var result = channelid?.hashCode() ?: 0
        result = 31 * result + (chaincodeid?.hashCode() ?: 0)
        result = 31 * result + cmd.hashCode()
        result = 31 * result + fcn.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }
}
