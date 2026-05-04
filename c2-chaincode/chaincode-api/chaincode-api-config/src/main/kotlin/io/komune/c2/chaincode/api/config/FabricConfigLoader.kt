package io.komune.c2.chaincode.api.config

import io.komune.c2.chaincode.dsl.ChannelId


class FabricConfigLoader(
	private val coopConfig: C2ChaincodeConfiguration
) {

	fun getChannelConfig(channelId: ChannelId): ChannelConfig {
		return coopConfig.getChannelChaincodes().get(channelId)
			?: throw ChannelConfigNotFoundException(channelId)
	}


	fun getFabricConfig(channelId: ChannelId): FabricConfig {
		val channelConfig = getChannelConfig(channelId)
		return FabricConfig.loadFromFile(channelConfig.config.file)
	}

}
