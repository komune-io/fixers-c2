package ssm.couchdb.f2.commons

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.burst
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId

fun chainCodeDbName(channelId: ChannelId, chaincodeId: ChaincodeId) = "${channelId}_$chaincodeId"

fun ChaincodeUriDTO.chainCodeDbName(): String {
	val uri = burst()
	return chainCodeDbName(uri.channelId, uri.chaincodeId)
}

fun ChaincodeUri.chainCodeDbName(): String {
	return chainCodeDbName(channelId, chaincodeId)
}
