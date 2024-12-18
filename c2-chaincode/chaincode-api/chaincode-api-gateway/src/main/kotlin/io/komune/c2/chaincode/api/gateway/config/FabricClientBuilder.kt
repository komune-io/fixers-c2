package io.komune.c2.chaincode.api.gateway.config

import io.komune.c2.chaincode.api.fabric.FabricChainCodeClient
import io.komune.c2.chaincode.api.fabric.FabricChainCodeClientSuspend
import io.komune.c2.chaincode.api.fabric.FabricChannelClient
import io.komune.c2.chaincode.api.fabric.FabricUserClient
import io.komune.c2.chaincode.api.fabric.config.FabricConfig
import io.komune.c2.chaincode.api.fabric.factory.FabricChannelFactory
import io.komune.c2.chaincode.api.fabric.factory.FabricClientFactory

class FabricClientBuilder(val coopConfig: HeraclesConfigProps) {

	fun getChannelConfig(channelId: ChannelId): ChannelChaincode {
		return coopConfig.getChannelChaincodes().get(channelId)
			?: throw ChannelConfigNotFoundException(channelId)
	}

	fun getFabricConfig(channelId: ChannelId): FabricConfig {
		val channelConfig = getChannelConfig(channelId)
		return FabricConfig.loadFromFile(channelConfig.config.file)
	}

	fun getFabricClientFactory(channelId: ChannelId): FabricClientFactory {
		val channelConfig = getChannelConfig(channelId)
		val fabricConfig = getFabricConfig(channelId)
		return FabricClientFactory.factory(fabricConfig, channelConfig.config.crypto)
	}

	fun getFabricChannelFactory(channelId: ChannelId): FabricChannelFactory {
		val channelConfig = getChannelConfig(channelId)
		val fabricConfig = getFabricConfig(channelId)
		return FabricChannelFactory.factory(fabricConfig, channelConfig.config.crypto)
	}

	fun getFabricChainCodeClient(channelId: ChannelId): FabricChainCodeClient {
		val fabricChannelFactory = getFabricChannelFactory(channelId)
		return FabricChainCodeClient(fabricChannelFactory)
	}

	fun getFabricChainCodeClientSuspend(channelId: ChannelId): FabricChainCodeClientSuspend {
		val fabricChannelFactory = getFabricChannelFactory(channelId)
		return FabricChainCodeClientSuspend(fabricChannelFactory)
	}

	fun getFabricChannelClient(channelId: ChannelId): FabricChannelClient {
		val fabricChannelFactory = getFabricChannelFactory(channelId)
		return FabricChannelClient(fabricChannelFactory)
	}

	fun getFabricUserClient(channelId: ChannelId): FabricUserClient {
		val fabricConfig = getFabricConfig(channelId)
		val fabricClientFactory = getFabricClientFactory(channelId)
		return FabricUserClient(fabricConfig, fabricClientFactory)
	}
}
