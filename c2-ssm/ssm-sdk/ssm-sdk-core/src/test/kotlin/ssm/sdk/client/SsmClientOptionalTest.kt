package ssm.sdk.client

import io.komune.c2.chaincode.dsl.ChaincodeUri
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SsmClientOptionalTest {

	private var client = SsmClientTestBuilder.build().buildQueryService()

	private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

	@Test
	fun adminUser(): Unit = runBlocking {
		val agentRet = client.getAdmin(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(agentRet).isNull()
	}

	@Test
	fun agentUser2(): Unit = runBlocking {
		val agentRet = client.getAgent(chaincodeUri,UUID.randomUUID().toString())
		Assertions.assertThat(agentRet).isNull()
	}

	@Test
	fun ssm(): Unit = runBlocking {
		val ssmReq = client.getSsm(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(ssmReq).isNull()
	}

	@Test
	fun session(): Unit = runBlocking {
		val ses = client.getSession(chaincodeUri, UUID.randomUUID().toString())
		Assertions.assertThat(ses).isNull()
	}
}
