package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.InvokeFunction

data class InvokeArgs(
	val function: InvokeFunction,
	val values: List<String>,
	/**
	 * Optional business timestamp (epoch millis). When set, [io.komune.c2.chaincode.api.fabric.FabricGatewayClient]
	 * rewrites the Fabric transaction envelope's ChannelHeader.timestamp to this value instead of using wall-clock
	 * time. Client-side metadata only: NOT part of the on-chain args nor the SSM signature.
	 */
	val timestamp: Long? = null,
) {
	constructor(function: String, vararg values: String) :
		this(InvokeFunction(function), values.toList())
}
