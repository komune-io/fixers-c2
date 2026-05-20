package ssm.sdk.client

import io.komune.c2.chaincode.dsl.Block
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.Transaction
import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.util.Lists
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.extention.addPrivateMessage
import ssm.sdk.sign.extention.getPrivateMessage
import ssm.sdk.sign.extention.loadFromFile
import ssm.sdk.sign.model.Signer
import ssm.sdk.sign.model.SignerAdmin
import ssm.sdk.sign.model.SignerUser

@TestMethodOrder(OrderAnnotation::class)
class SsmClientItTest {

	companion object {
		private val uuid = UUID.randomUUID().toString()
		private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
		private const val NETWORK = "bclan-it/"
		const val ADMIN_NAME = "ssm-admin"
		val USER1_NAME = "bob-$uuid"
		private val USER2_NAME = "sam-$uuid"
		const val USER1_FILENAME = NETWORK + "bob"
		private const val USER2_FILENAME = NETWORK + "sam"

		private lateinit var query: SsmQueryService
		private lateinit var tx: SsmTxService
		private lateinit var ssmName: String
		private lateinit var sessionName: String
		private lateinit var session: SsmSession


		private var signerAdmin: SignerAdmin = SignerAdmin.loadFromFile(ADMIN_NAME, NETWORK + ADMIN_NAME)
		private var signerUser1: Signer = SignerUser.loadFromFile(USER1_NAME, USER1_FILENAME)
		private var signerUser2: Signer = SignerUser.loadFromFile(USER2_NAME, USER2_FILENAME)

		private val signer = SsmCmdSignerSha256RSASigner(
			SignerAdmin.loadFromFile(ADMIN_NAME, NETWORK + ADMIN_NAME),
			SignerUser.loadFromFile(USER1_NAME, USER1_FILENAME),
			SignerUser.loadFromFile(USER2_NAME, USER2_FILENAME)
		)

		private var agentAdmin: Agent = Agent.loadFromFile(ADMIN_NAME, NETWORK + ADMIN_NAME)
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

	@Order(5)
	@Test
	fun listAdmin() = runTest {
		val agentRet = query.listAdmins(chaincodeUri)
		Assertions.assertThat(agentRet).contains(ADMIN_NAME)
	}

	@Order(10)
	@Test
	fun adminUser() = runTest {
		val agentFormClient = query.getAdmin(chaincodeUri, ADMIN_NAME)
		Assertions.assertThat(agentFormClient).isEqualTo(agentAdmin)
	}

	@Test
	@Order(20)
	fun registerUser1() = runTest {
		val transactionEvent = tx.sendRegisterUser(chaincodeUri, agentUser1, signerAdmin.name)
		assertThatTransactionExists(transactionEvent)
	}

	@Order(30)
	@Test
	fun agentUser1() = runTest {
		val agentRet = query.getAgent(chaincodeUri, agentUser1.name)!!
		Assertions.assertThat(agentRet).isEqualTo(agentUser1)
	}

	@Test
	@Order(40)
	fun registerUser2() = runTest {
		val transactionEvent = tx.sendRegisterUser(chaincodeUri, agentUser2, signerAdmin.name)
		assertThatTransactionExists(transactionEvent)
	}

	@Order(50)
	@Test
	fun agentUser2() = runTest {
		val agentRet = query.getAgent(chaincodeUri, agentUser2.name)
		Assertions.assertThat(agentRet).isEqualTo(agentUser2)
	}

	@Test
	@Order(55)
	fun listAgent() = runTest {
		val agentRet = query.listUsers(chaincodeUri)
		Assertions.assertThat(agentRet).contains(agentUser1.name, agentUser2.name)
	}

	@Test
	@Order(60)
	fun createSsm() = runTest {
		val sell = SsmTransition(0, 1, "Seller", "Sell")
		val buy = SsmTransition(1, 2, "Buyer", "Buy")
		val ssm = Ssm(ssmName, Lists.newArrayList(sell, buy))
		val transactionEvent = tx.sendCreate(chaincodeUri, ssm, signerAdmin.name)
		assertThatTransactionExists(transactionEvent)
	}

	@Order(70)
	@Test
	fun ssm() = runTest {
		val ssmReq = query.getSsm(
			chaincodeUri,
			ssmName
		)
		Assertions.assertThat(ssmReq).isNotNull
		Assertions.assertThat(ssmReq!!.name).isEqualTo(ssmName)
	}

	@Test
	@Order(80)
	fun start() = runTest {
		val roles: Map<String, String> = mapOf(
			agentUser1.name to "Buyer", agentUser2.name to "Seller"
		)
		val session = SsmSession(
			ssmName,
			sessionName, roles, "Used car for 100 dollars.", emptyMap()
		)
		val transactionEvent = tx.sendStart(chaincodeUri, session, signerAdmin.name)
		assertThatTransactionExists(transactionEvent)
	}

	@Order(90)
	@Test
	fun session() = runTest {
		val ses = query.getSession(
			chaincodeUri,
			sessionName
		)

		Assertions.assertThat(ses?.current).isEqualTo(0)
		Assertions.assertThat(ses?.iteration).isEqualTo(0)
		Assertions.assertThat(ses?.origin).isNull()
		Assertions.assertThat(ses?.ssm).isEqualTo(ssmName)
		Assertions.assertThat(ses?.roles).isEqualTo(session.roles)
		Assertions.assertThat(ses?.session).isEqualTo(session.session)
		Assertions.assertThat(ses?.public).isEqualTo(session.public)
	}

	@Test
	@Order(100)
	fun performSell() = runTest {
		var context = SsmContext(sessionName, "100 dollars 1978 Camaro", 0, emptyMap())
		context = context.addPrivateMessage(
			"Message to signer1",
			agentUser1
		)
		privateMessage = context.private
		val transactionEvent = tx.sendPerform(chaincodeUri,"Sell", context, signerUser2.name)
		assertThatTransactionExists(transactionEvent)
	}

	@Order(110)
	@Test
	fun sessionAfterSell() = runTest {
		val sell = SsmTransition(0, 1, "Seller", "Sell")
		val sesReq = query.getSession(
			chaincodeUri,
			sessionName
		)
		val stateExpected = SsmSessionState(
			ssmName,
			sessionName, session.roles, "100 dollars 1978 Camaro", privateMessage, sell, 1, 1
		)
		Assertions.assertThat(sesReq).isEqualTo(stateExpected)
	}

	@Order(110)
	@Test
	fun sessionAfterSellShouldReturnEncryptMessage() = runTest {
//		val (from, to, role, action) = SsmTransition(0, 1, "Seller", "Sell")
		val state = query.getSession(chaincodeUri, sessionName)
		val expectedMessage = state?.getPrivateMessage(signerUser1)
		Assertions.assertThat(expectedMessage).isEqualTo("Message to signer1")
	}

	@Test
	@Order(120)
	fun performBuy() = runTest {
		val context = SsmContext(sessionName, "Deal !", 1, emptyMap())
		val transactionEvent = tx.sendPerform(chaincodeUri,"Buy", context, signerUser1.name)
		assertThatTransactionExists(transactionEvent)
	}

	private suspend fun assertThatTransactionExists(trans: InvokeReturn) {
		Assertions.assertThat(trans).isNotNull
		Assertions.assertThat(trans.status).isEqualTo("SUCCESS")
		val transaction: Transaction? = query.getTransaction(chaincodeUri, trans.transactionId)
		Assertions.assertThat(transaction).isNotNull
		Assertions.assertThat(transaction?.blockId).isNotNull
		val block: Block? = query.getBlock(chaincodeUri, transaction!!.blockId)
		Assertions.assertThat(block).isNotNull
	}

	@Order(130)
	@Test
	fun sessionAfterBuy() = runTest {
		val buy = SsmTransition(1, 2, "Buyer", "Buy")
		val state = query.getSession(
			chaincodeUri,
			sessionName
		)
		val stateExpected = SsmSessionState(
			ssmName,
			sessionName, session.roles, "Deal !", emptyMap(), buy, 2, 2
		)
		Assertions.assertThat(state).isEqualTo(stateExpected)
	}

	@Test
	@Order(135)
	@Throws(Exception::class)
	fun logSession() = runTest {
		val sesReq = query.log(
			chaincodeUri,
			sessionName
		)
		Assertions.assertThat(sesReq.size).isEqualTo(3)
	}

	@Test
	@Order(140)
	fun listSsm() = runTest {
		val agentRet = query.listSsm(chaincodeUri)
		Assertions.assertThat(agentRet).contains(ssmName)
	}

	@Test
	@Order(150)
	fun listSession() = runTest {
		val agentRet = query.listSession(chaincodeUri)
		Assertions.assertThat(agentRet).contains(sessionName)
	}
}
