package ssm.sdk.client

import io.komune.c2.chaincode.dsl.ChaincodeUri
import java.util.UUID
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SsmClientOptionalTest {

	private var client = SsmClientTestBuilder.build().buildQueryService()

	private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

	@Test
	suspend fun adminUser() {
		val agentRet = client.getAdmin(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(agentRet).isNull()
	}

	@Test
	suspend fun agentUser2() {
		val agentRet = client.getAgent(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(agentRet).isNull()
	}

	@Test
	suspend fun ssm() {
		val ssmReq = client.getSsm(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(ssmReq).isNull()
	}

	@Test
	suspend fun session() {
		val ses = client.getSession(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(ses).isNull()
	}
}
