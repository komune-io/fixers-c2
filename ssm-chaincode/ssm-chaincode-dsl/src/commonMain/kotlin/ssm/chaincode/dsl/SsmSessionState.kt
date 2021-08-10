package ssm.chaincode.dsl

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName

expect interface SsmSessionStateDTO: SsmSessionDTO, WithPrivate  {
	override val ssm: String?
	override val session: String
	override val roles: Map<String, String>?
	override val public: Any?
	override val private: Map<String, String>?

	/**
	 * Transition that lead to the current state
	 */
	val origin: SsmTransitionDTO?

	/**
	 * Current state identifier
	 * @example 2
	 */
	val current: Int

	/**
	 * Number of iterations the session has undergone before attaining the current state
	 * @example 3
	 */
	val iteration: Int
}

/**
 * The Session State represents a snapshot of a session on a given state machine. It holds the current state index, public and private data relevant to the SSM session.
 * The iteration is incremented at every transition. The originating transition allows to track the session history in the ledger.
 * @d2 model
 * @parent [SsmSession]
 * @order 10
 */
@Serializable
@JsExport
@JsName("SsmSessionState")
data class SsmSessionState(
	override val ssm: String?,
	override val session: String,
	override val roles: Map<String, String>?,
	override val public: Any?,
	override val private: Map<String, String>? = hashMapOf(),
	override val origin: SsmTransition?,
	override val current: Int,
	override val iteration: Int,
): SsmSessionStateDTO