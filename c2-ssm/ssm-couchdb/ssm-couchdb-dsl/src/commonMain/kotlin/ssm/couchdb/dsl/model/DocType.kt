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
sealed class DocType<T : Any>(
    val name: DocTypeName,
    val clazz: KClass<T>,
) {
    object Admin : DocType<Agent>("admin", Agent::class)
    object User : DocType<Agent>("user", Agent::class)
    object Grant : DocType<SsmGrant>("grant", SsmGrant::class)
    object Ssm : DocType<ssm.chaincode.dsl.model.Ssm>("ssm", ssm.chaincode.dsl.model.Ssm::class)
    object State : DocType<SsmSessionState>("state", SsmSessionState::class)
    object Chaincode : DocType<ChaincodeLscc>("state", ChaincodeLscc::class)
}

@Serializable
@JsExport
@JsName("ChaincodeLscc")
@Suppress("ConstructorParameterNaming")
class ChaincodeLscc(
    val _id: ChaincodeId,
    val _rev: String,
)
