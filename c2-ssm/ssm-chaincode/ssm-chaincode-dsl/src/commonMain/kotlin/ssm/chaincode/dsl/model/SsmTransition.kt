package ssm.chaincode.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

typealias SsmAction = String
typealias SsmRole = String

@JsExport
@JsName("SsmTransitionDTO")
interface SsmTransitionDTO {
    /**
     * Origin of the transition.
     * @example 1
     */
    val from: Int

    /**
     * Destination of the transition.
     * @example 2
     */
    val to: Int

    /**
     * Role of the [agent][Agent] allowed to trigger the transition.
     * @example "Seller"
     */
    val role: SsmRole

    /**
     * Trigger of the transition.
     * @example "Sell"
     */
    val action: SsmAction
}

/**
 * @D2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 */
@Serializable
@JsExport
@JsName("SsmTransition")
data class SsmTransition(
    override val from: Int,
    override val to: Int,
    override val role: SsmRole,
    override val action: SsmAction,
) : SsmTransitionDTO
