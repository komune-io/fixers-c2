package io.komune.c2.chaincode.dsl

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

@JsExport
@JsName("ChaincodeUriDTO")
interface ChaincodeUriDTO {
	val uri: String
}

fun ChaincodeUriDTO.burst() = ChaincodeUri(uri)

@Serializable
@JsExport
@JsName("ChaincodeUri")
data class ChaincodeUri(override val uri: String): ChaincodeUriDTO {

	companion object {
		const val PARTS = 3
		const val PREFIX = "chaincode"
	}

	private val burst = uri.split(":")
	init {
		require(burst.size == PARTS)
		require(burst.first() == PREFIX)
	}

	val channelId
		get() = burst[1]
	val chaincodeId
		get() = burst[2]

}

fun ChaincodeUri.Companion.from(channelId: ChannelId?, chaincodeId: ChaincodeId?): ChaincodeUri {
	return ChaincodeUri("chaincode:$channelId:$chaincodeId")
}
