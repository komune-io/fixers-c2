package ssm.chaincode.dsl

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName

expect interface SsmSessionDTO: WithPrivate {
	/**
	 * Identifier of the instantiated [SSM][Ssm]
	 * @example [Ssm.name]
	 */
	val ssm: String?

	/**
	 * Identifier of the session
	 * @example "eca7c042-ec37-489b-adb8-42c73ddcfb0b"
	 */
	val session: String

	/**
	 * Associate each role defined in the SSM to an agent
	 * @example {
	 * 	"Provider": "JohnDeuf",
	 * 	"Seller": "BenEfficiere",
	 * 	"Buyer": "JeanneAlyztou"
	 * }
	 */
	val roles: Map<String, String>?

	/**
	 * Public data attached to the session
	 * @example "The seller is a scam"
	 */
	val public: Any?

	/**
	 * Private data attached to the session
	 * @example {
	 * 	"cake": "lie"
	 * }
	 */
	override val private: Map<String, String>?
}

/**
 * @d2 model
 * @page
 * While an [SSM][Ssm] purely describes the structure of a State Machine, a session represents its instantiation.
 * It defines which [agent][SsmAgent] is assigned to which role, and keeps track of every state transition it has undergone.
 * @@title SSM-Chaincode/Session
 */
@Serializable
@JsExport
@JsName("SsmSession")
open class SsmSession(
	override val ssm: String,
	override val session: String,
	override val roles: Map<String, String>,
	override val public: String,
	override val private: Map<String, String>? = hashMapOf(),
): SsmSessionDTO

