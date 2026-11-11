package ssm.chaincode.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

@JsExport
@JsName("SsmSessionDTO")
interface SsmSessionDTO : WithPrivate {
    /**
     * Identifier of the instantiated [SSM][Ssm].
     * @example [Ssm.name]
     */
    val ssm: String?

    /**
     * Identifier of the session.
     * @example "eca7c042-ec37-489b-adb8-42c73ddcfb0b"
     */
    val session: String

    /**
     * Associate each role defined in the SSM to an agent.
     * @example {
     * 	"Provider": "JohnDeuf",
     * 	"Seller": "BenEfficiere",
     * 	"Buyer": "JeanneAlyztou"
     * }
     */
    val roles: Map<String, String>?

    /**
     * Public data attached to the session.
     * @example "The seller is a scam"
     */
    val public: Any?

    /**
     * Private data attached to the session.
     * @example {
     * 	"cake": "lie"
     * }
     */
    override val private: Map<String, String>?
}

/**
 * @d2 model
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Model]
 * @title SSM-CHAINCODE/Session
 * While an [SSM][Ssm] purely describes the structure of a State Machine, a session represents its instantiation.
 * It defines which [agent][Agent] is assigned to which role,
 * and keeps track of every state transition it has undergone.
 */
@Serializable
@JsExport
@JsName("SsmSession")
open class SsmSession(
    override val ssm: SsmName,
    override val session: SessionName,
    override val roles: Map<String, String>,
    override val public: String,
    override val private: Map<String, String>? = hashMapOf(),
) : SsmSessionDTO
