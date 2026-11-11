package ssm.data.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChannelId

@JsExport
@JsName("DataChannelDTO")
interface DataChannelDTO {
    /**
     * Identifier.
     * @example "channel-@komune-io"
     */
    val id: ChannelId
}

/**
 * Description of a channel.
 * @d2 model
 * @parent [ssm.data.dsl.DataSsmD2Model]
 */
@Serializable
@JsExport
@JsName("DataChannel")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataChannel(
    override val id: ChannelId,
) : DataChannelDTO
