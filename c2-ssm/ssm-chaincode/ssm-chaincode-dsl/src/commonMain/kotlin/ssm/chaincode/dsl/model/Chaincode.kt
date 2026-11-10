package ssm.chaincode.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

/**
 * The unique id of a chaincode.
 * @d2 model
 * @example "ssm"
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 */
typealias ChaincodeId = String

@JsExport
@JsName("ChaincodeDTO")
interface ChaincodeDTO {
    val id: ChaincodeId
    val channelId: ChannelId
}

@Serializable
@JsExport
@JsName("Chaincode")
data class Chaincode(
    override val id: ChaincodeId,
    override val channelId: ChannelId,
) : ChaincodeDTO
