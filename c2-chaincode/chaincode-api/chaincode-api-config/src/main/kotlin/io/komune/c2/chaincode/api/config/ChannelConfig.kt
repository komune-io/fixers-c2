package io.komune.c2.chaincode.api.config

import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.Endorser


class ChannelConfig(
    val channelId: ChannelId,
    val chaincodeId: List<ChaincodeId>,
    val user: C2ChaincodeConfiguration.UserConfig,
    val config: C2ChaincodeConfiguration.FileConfig,
    val endorsers: List<Endorser>
) {
    companion object
}

data class ChannelChaincodePair(
    val channelId: ChannelId,
    val chainCodeId: ChaincodeId
) {
    companion object {
        fun fromConfig(defaultValue: String): ChannelChaincodePair {
            val ccidByChannel = defaultValue.split("/")
            require(ccidByChannel.size == 2) {
                "Bad ccid argument[${defaultValue}]. Syntax must by channelId/chaincodeId"
            }
            return ChannelChaincodePair(
                channelId = ccidByChannel[0],
                chainCodeId = ccidByChannel[1]
            )
        }
    }
}



fun ChannelConfig.Companion.fromConfig(
    lines: Array<String>,
    user: C2ChaincodeConfiguration.UserConfig,
    config: C2ChaincodeConfiguration.FileConfig,
    endorsers: List<Endorser>
): Map<ChannelId, ChannelConfig> {
    return lines.map { line ->
        ChannelChaincodePair.fromConfig(line)
    }.groupBy(
        { it.channelId }, { it.chainCodeId }
    ).mapValues { (channelId, chaincodeIds) ->
        ChannelConfig(
            channelId = channelId,
            chaincodeId = chaincodeIds,
            user = user,
            config = config,
            endorsers = endorsers
        )

    }
}

class ChannelConfigNotFoundException(channelId: ChannelId) : Exception(channelId)
