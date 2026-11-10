package ssm.chaincode.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

@JsExport
@JsName("SsmContextDTO")
interface SsmContextDTO : WithPrivate {
    /**
     * Described session
     * @example [SsmSession.session]
     */
    val session: String

    /**
     * Public data attached to the session for this iteration
     * @example [SsmSession.public]
     */
    val public: String

    /**
     * Described iteration
     * @example [SsmSessionState.iteration]
     */
    val iteration: Int

    /**
     * Private data attached to the session
     * @example [SsmSession.private]
     */
    override val private: Map<String, String>?
}

/**
 * Describes a session context for a given iteration
 * @d2 model
 * @parent [SsmSession]
 */
@Serializable
@JsExport
@JsName("SsmContext")
data class SsmContext(
    override val session: String,
    override val public: String,
    override val iteration: Int,
    override val private: Map<String, String>? = null,
) : SsmContextDTO
