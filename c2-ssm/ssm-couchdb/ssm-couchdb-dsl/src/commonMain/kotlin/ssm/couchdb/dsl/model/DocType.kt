package ssm.couchdb.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.SsmGrant
import ssm.chaincode.dsl.model.SsmSessionState

typealias DocTypeName = String

@Serializable
@JsExport
@JsName("DocType")
sealed class DocType<out T : Any>(
    val name: DocTypeName,
) {
    /** The class a document of this type is read as; Kotlin only, JS has no class to read into. */
    @JsExport.Ignore
    abstract val clazz: KClass<out T>

    object Admin : DocType<Agent>("admin") {
        @JsExport.Ignore
        override val clazz = Agent::class
    }
    object User : DocType<Agent>("user") {
        @JsExport.Ignore
        override val clazz = Agent::class
    }
    object Grant : DocType<SsmGrant>("grant") {
        @JsExport.Ignore
        override val clazz = SsmGrant::class
    }
    object Ssm : DocType<ssm.chaincode.dsl.model.Ssm>("ssm") {
        @JsExport.Ignore
        override val clazz = ssm.chaincode.dsl.model.Ssm::class
    }
    object State : DocType<SsmSessionState>("state") {
        @JsExport.Ignore
        override val clazz = SsmSessionState::class
    }
    object Chaincode : DocType<ChaincodeLscc>("state") {
        @JsExport.Ignore
        override val clazz = ChaincodeLscc::class
    }
}

@Serializable
@JsExport
@JsName("ChaincodeLscc")
// _id and _rev are the CouchDB field names; @JsExport: a data class would add copy and componentN to JS.
@Suppress("ConstructorParameterNaming", "UseDataClass")
class ChaincodeLscc(
    val _id: ChaincodeId,
    val _rev: String,
)
