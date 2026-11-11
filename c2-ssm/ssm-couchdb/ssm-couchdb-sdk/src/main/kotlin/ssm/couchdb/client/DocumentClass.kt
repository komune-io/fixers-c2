package ssm.couchdb.client

import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmGrant
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.couchdb.dsl.model.ChaincodeLscc
import ssm.couchdb.dsl.model.DocType

/**
 * The class a document of this type is read into. It lives on the JVM side: the type itself is exported to JS,
 * which has no class to read into.
 */
// Each branch is the class of that object's own type argument: the sealed `when` keeps every pair in step.
@Suppress("UNCHECKED_CAST")
internal val <T : Any> DocType<T>.documentClass: Class<T>
    get() = when (this) {
        DocType.Admin, DocType.User -> Agent::class.java
        DocType.Grant -> SsmGrant::class.java
        DocType.Ssm -> Ssm::class.java
        DocType.State -> SsmSessionState::class.java
        DocType.Chaincode -> ChaincodeLscc::class.java
    } as Class<T>
