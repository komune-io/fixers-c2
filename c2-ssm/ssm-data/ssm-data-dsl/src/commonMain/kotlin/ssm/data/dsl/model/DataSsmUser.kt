package ssm.data.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentDTO

@JsExport
@JsName("DataSsmUserDTO")
interface DataSsmUserDTO {
    /**
     * Agent able to interact with an SSM.
     */
    val agent: AgentDTO
}

/**
 * Represents an [SSM Agent][Agent] with metadata.
 * @d2 model
 * @parent [ssm.data.dsl.DataSsmD2Model]
 *
 */
@Serializable
@JsExport
@JsName("DataSsmUser")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmUser(
    override val agent: Agent,
) : DataSsmUserDTO
