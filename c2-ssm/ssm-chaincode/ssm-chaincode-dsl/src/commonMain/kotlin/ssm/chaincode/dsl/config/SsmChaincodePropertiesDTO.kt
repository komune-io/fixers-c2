package ssm.chaincode.dsl.config

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

/**
 * The configuration needed for reaching the SSM chaincode.
 * @d2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 */
@JsExport
@JsName("SsmChaincodePropertiesDTO")
interface SsmChaincodePropertiesDTO {
    /**
     * URL of the peer hosting the chaincode.
     * @example "http://peer.sandbox.Komune.io:9000"
     */
    val url: String
}

// A data class would add copy and componentN to the exported JS surface.
@Suppress("UseDataClass")
@Serializable
@JsExport
@JsName("ChaincodeSsmConfig")
class SsmChaincodeProperties(
    override val url: String,
) : SsmChaincodePropertiesDTO
