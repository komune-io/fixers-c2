package ssm.couchdb.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmGrant
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.couchdb.dsl.model.ChaincodeLscc
import ssm.couchdb.dsl.model.DocType

/** Each document type is read into the class of its own type argument. Pure mapping: no server needed. */
class DocumentClassTest {

    @Test
    fun `every document type names the class its documents are read into`() {
        val byType = mapOf<DocType<*>, Class<*>>(
            DocType.Admin to Agent::class.java,
            DocType.User to Agent::class.java,
            DocType.Grant to SsmGrant::class.java,
            DocType.Ssm to Ssm::class.java,
            DocType.State to SsmSessionState::class.java,
            DocType.Chaincode to ChaincodeLscc::class.java,
        )

        assertThat(byType.mapValues { (type, _) -> type.documentClass }).isEqualTo(byType)
    }
}
