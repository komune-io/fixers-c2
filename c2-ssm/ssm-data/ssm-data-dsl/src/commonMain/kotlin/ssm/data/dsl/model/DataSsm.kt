package ssm.data.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmDTO
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.chaincode.dsl.model.uri.SsmVersion

@JsExport
@JsName("DataSsmDTO")
interface DataSsmDTO {
    val uri: SsmUriDTO
    /**
     * Description of a state machine
     */
    val ssm: SsmDTO

    /**
     * Channel in which the SSM has been instantiated
     */
    val channel: DataChannelDTO

    /**
     * Version of the SSM
     * @example "1.0"
     */
    val version: SsmVersion?
}

/**
 * @d2 model
 * @parent [ssm.data.dsl.DataSsmD2Model]
 * Represents an [SSM][Ssm] with metadata
 * title DataSSM
 */
@Serializable
@JsExport
@JsName("DataSsm")
class DataSsm(
    override val uri: SsmUri,
    override val ssm: Ssm,
    override val channel: DataChannel,
    override val version: SsmVersion?,
) : DataSsmDTO
