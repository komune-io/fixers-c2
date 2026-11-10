package ssm.chaincode.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

typealias AgentName = String

@JsExport
@JsName("AgentDTO")
interface AgentDTO {
    /**
     * Identifier of the agent
     * @example "Adam"
     */
    val name: AgentName

    /**
     * Public key used when signing transactions
     * @example "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3MO+2nIRi/cs4WbE+ykzA1ErTfs0QmBIdpZoAsU7YVMKBnBNulxhy2BI93QHK9uQreLhANBDexagMZg9ZzCxtKLi9UNHSm08099znPfMKn2cITHI8ShyZC7OogsbNmqrY0iy01r4IVpPi4CMNhLTCWyLGWS+L0hsmZOQQWV5BeER4nufBgGmA8plD14T/AXaHF7pMJAGlvauqjcjhb9YAoDUjSmdy4h3KzNq0c1KSQwORgQhgGItUxs5X8jvAXsikRDs7OkqbEDWpSf5z6FSyenvPmnplrqL/5bjiis6ObbOA+BjpMpyuouXOA3WuGv61a5Wrx62bcfeCx9471EKFQIDAQAB"
     */
    val pub: ByteArray
}

/**
 * @d2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 * An Agent is a user able to interact with an SSM it has been registered for.
 * It holds a pair of public/private keys which will be used to sign transactions.
 *
 * There are two kind of agents:
 *  - **Users** are the agents who can trigger a transition in an SSM.
 *  In effect, they are the participants in the smart contract represented by the SSM.
 *  - **Admins** have the rights to add new users and state machines. They are declared only once,
 *  when the chaincode is instantiated.
 * @title SSM-CHAINCODE/Agent
 */
@Serializable
@JsExport
@JsName("Agent")
data class Agent(
    override val name: AgentName,
    override val pub: ByteArray,
) : AgentDTO {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Agent

        if (name != other.name) return false
        if (!pub.contentEquals(other.pub)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + pub.contentHashCode()
        return result
    }
    companion object
}
