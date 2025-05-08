package ssm.sdk.client

import io.komune.c2.chaincode.dsl.ChaincodeUri
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes
import org.assertj.core.util.Lists
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.sdk.dsl.InvokeException
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.extention.addPrivateMessage
import ssm.sdk.sign.extention.loadFromFile
import ssm.sdk.sign.model.Signer
import ssm.sdk.sign.model.SignerAdmin
import ssm.sdk.sign.model.SignerUser

@TestMethodOrder(OrderAnnotation::class)
class SsmClientErrorTest {

	companion object {
		private val uuid = UUID.randomUUID().toString()
		private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
		private const val NETWORK = "bclan-it/"
		const val ADMIN_NAME = "ssm-admin"
		val USER1_NAME = "bob-$uuid"
		val USER2_NAME = "sam-$uuid"
		const val USER1_FILENAME = NETWORK + "bob"
		const val USER2_FILENAME = NETWORK + "sam"

		private lateinit var query: SsmQueryService
		private lateinit var tx: SsmTxService
		private lateinit var ssmName: String
		private lateinit var sessionName: String
		private lateinit var session: SsmSession


		private var signerAdmin: SignerAdmin = SignerAdmin.loadFromFile(ADMIN_NAME, NETWORK + ADMIN_NAME)
		private var signerUser1: Signer = SignerUser.loadFromFile(USER1_NAME, USER1_FILENAME)
		private var signerUser2: Signer = SignerUser.loadFromFile(USER2_NAME, USER2_FILENAME)

		val signer = SsmCmdSignerSha256RSASigner(
			SignerAdmin.loadFromFile(ADMIN_NAME, NETWORK + ADMIN_NAME),
			SignerUser.loadFromFile(USER1_NAME, USER1_FILENAME),
			SignerUser.loadFromFile(USER2_NAME, USER2_FILENAME)
		)

		private var agentUser1: Agent = Agent.loadFromFile(signerUser1.name, USER1_FILENAME)
		private var agentUser2: Agent = Agent.loadFromFile(signerUser2.name, USER2_FILENAME)


		@BeforeAll
		@JvmStatic
		@Throws(Exception::class)
		fun init() {
			query = SsmClientTestBuilder.build().buildQueryService()
			tx = SsmClientTestBuilder.build().buildTxService(signer)
			ssmName = "CarDealership-$uuid"
			val roles = mapOf(
				signerUser1.name to "Buyer", signerUser2.name to "Seller"
			)
			sessionName = "deal20181201-$uuid"
			session = SsmSession(
				ssmName,
				sessionName, roles, "Used car for 100 dollars.", emptyMap()
			)
		}

		private var privateMessage: Map<String, String>? = null
	}

	@Suppress("LongMethod")
	@Test
	fun fullTest() = runTest {
		println("//////////////////////////////")
		println("registerUser1")
		tx.sendRegisterUser(chaincodeUri, agentUser1, signerAdmin.name)
		// Catch the exception from the second call
		val tt = assertThatThrowable {
			tx.sendRegisterUser(
				chaincodeUri,
				agentUser1,
				signerAdmin.name
			)
		}
		tt.isInstanceOf(InvokeException::class.java)
		tt.hasMessage("Identifier USER_${agentUser1.name} already in use.")


		println("//////////////////////////////")
		println("registerUser2")
		tx.sendRegisterUser(chaincodeUri, agentUser2, signerAdmin.name)
		assertThatThrowable {
			tx.sendRegisterUser(chaincodeUri, agentUser2, signerAdmin.name)
		}.isInstanceOf(InvokeException::class.java)
			.hasMessage("Identifier USER_${agentUser2.name} already in use.")

		println("//////////////////////////////")
		println("createSsm")
		val sell = SsmTransition(0, 1, "Seller", "Sell")
		val buy = SsmTransition(1, 2, "Buyer", "Buy")
		val ssm = Ssm(ssmName, Lists.newArrayList(sell, buy))
		tx.sendCreate(chaincodeUri, ssm, signerAdmin.name)

		assertThatThrowable {
			tx.sendCreate(chaincodeUri, ssm, signerAdmin.name)
		}.isInstanceOf(InvokeException::class.java)
			.hasMessage("Identifier SSM_${ssmName} already in use.")

		println("//////////////////////////////")
		println("startSession")
		val roles: Map<String, String> = mapOf(
			agentUser1.name to "Buyer", agentUser2.name to "Seller"
		)
		val session = SsmSession(
			ssmName,
			sessionName, roles, "Used car for 100 dollars.", emptyMap()
		)
		tx.sendStart(chaincodeUri, session, signerAdmin.name)

		assertThatThrowable {
			tx.sendStart(chaincodeUri, session, signerAdmin.name)
		}.isInstanceOf(InvokeException::class.java)
			.hasMessage("Identifier STATE_${sessionName} already in use.")


		println("//////////////////////////////")
		println("performSell")
		var sellcontext = SsmContext(sessionName, "100 dollars 1978 Camaro", 0, emptyMap()).apply {  }
		sellcontext = sellcontext.addPrivateMessage(
			"Message to signer1",
			agentUser1
		)
		privateMessage = sellcontext.private
		tx.sendPerform(chaincodeUri,"Sell", sellcontext, signerUser2.name)

		assertThatThrowable {
			tx.sendPerform(chaincodeUri,"Sell", sellcontext, signerUser2.name)
		}
			.isInstanceOf(InvokeException::class.java)
			.hasMessage("No valid transition from state ")

		println("//////////////////////////////")
		println("performBuy")
		val buyContext = SsmContext(sessionName, "Deal !", 1, emptyMap())
		tx.sendPerform(chaincodeUri,"Buy", buyContext, signerUser1.name)

		assertThatThrowable {
			tx.sendPerform(chaincodeUri,"Buy", buyContext, signerUser1.name)
		}.isInstanceOf(InvokeException::class.java)
			.hasMessage("No valid transition from state ")
	}

}


fun assertThatThrowable(exec : suspend () -> Unit): AbstractThrowableAssert<*, Throwable> {
	val throwable = AssertionsForClassTypes.catchThrowable{
		runBlocking { exec() }
	}
	return Assertions.assertThat(throwable)
}
