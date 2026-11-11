package ssm.couchdb.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.SsmGrant
import ssm.chaincode.dsl.model.SsmSessionState

typealias DocTypeName = String

/** A CouchDB document type, by the name its documents carry; the JVM client reads each into its own class. */
@Serializable
@JsExport
@JsName("DocType")
sealed class DocType<out T : Any>(
    val name: DocTypeName,
) {
    object Admin : DocType<Agent>("admin")
    object User : DocType<Agent>("user")
    object Grant : DocType<SsmGrant>("grant")
    object Ssm : DocType<ssm.chaincode.dsl.model.Ssm>("ssm")
    object State : DocType<SsmSessionState>("state")
    object Chaincode : DocType<ChaincodeLscc>("state")
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
