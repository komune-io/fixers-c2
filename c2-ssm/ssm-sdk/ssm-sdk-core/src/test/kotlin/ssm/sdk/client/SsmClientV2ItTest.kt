package ssm.sdk.client

import io.komune.c2.chaincode.dsl.ChaincodeUri
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
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
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmPerformCommandV2
import ssm.sdk.core.command.SsmStartCommandV2
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.extention.loadFromFile
import ssm.sdk.sign.model.Signer
import ssm.sdk.sign.model.SignerAdmin
import ssm.sdk.sign.model.SignerUser

/**
 * Live-Fabric integration test for the full v2 SDK chain:
 * SsmTxService.sendStartV2 / sendPerformV2
 *   → SsmService.invokeAllV2
 *   → SsmRequester.invokeAllV2
 *   → KtorRepository.invokeV2
 *   → POST /invoke/v2
 *   → live chaincode-api-gateway
 *   → live Fabric peer + chaincode commit
 *
 * Pre-req: sandbox running at http://localhost:9090 (make build && make dev up).
 */
@TestMethodOrder(OrderAnnotation::class)
class SsmClientV2ItTest {

    companion object {
        private val uuid: String = UUID.randomUUID().toString()
        private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
        private const val NETWORK = "bclan-it/"
        private const val ADMIN_NAME = "ssm-admin"
        private val USER1_NAME = "bob-$uuid"
        private val USER2_NAME = "sam-$uuid"
        private const val USER1_FILENAME = NETWORK + "bob"
        private const val USER2_FILENAME = NETWORK + "sam"

        private lateinit var query: SsmQueryService
        private lateinit var tx: SsmTxService
        private lateinit var ssmName: String

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
        fun init() {
            query = SsmClientTestBuilder.build().buildQueryService()
            tx = SsmClientTestBuilder.build().buildTxService(signer)
            ssmName = "CarDealershipV2-$uuid"
        }
    }

    // ------------------------------------------------------------------ //
    //  Bootstrap: register agents + create SSM (using v1 helpers so the  //
    //  SSM exists on chain before the v2 tests run)                       //
    // ------------------------------------------------------------------ //

    @Order(10)
    @Test
    fun registerAdmin() = runTest {
        // Admin name is static (tied to signing identity), so it may already be registered
        // on a long-lived sandbox from prior runs. Either outcome is acceptable.
        val result = tx.sendRegisterUser(chaincodeUri, agentAdmin, signerAdmin.name)
        assertThat(result).isNotNull
        if (result.status != "SUCCESS") {
            assertThat(result.status).isEqualTo("ERROR")
            assertThat(result.info).contains("Identifier USER_${agentAdmin.name} already in use.")
        }
    }

    @Order(20)
    @Test
    fun registerUsers() = runTest {
        val r1 = tx.sendRegisterUser(chaincodeUri, agentUser1, signerAdmin.name)
        val r2 = tx.sendRegisterUser(chaincodeUri, agentUser2, signerAdmin.name)
        assertThat(r1.status).isEqualTo("SUCCESS")
        assertThat(r2.status).isEqualTo("SUCCESS")
    }

    @Order(30)
    @Test
    fun createSsm() = runTest {
        val sell = SsmTransition(0, 1, "Seller", "Sell")
        val buy = SsmTransition(1, 2, "Buyer", "Buy")
        val ssm = Ssm(ssmName, Lists.newArrayList(sell, buy))
        val result = tx.sendCreate(chaincodeUri, ssm, signerAdmin.name)
        assertThat(result).isNotNull
        assertThat(result.status).isEqualTo("SUCCESS")
    }

    // ------------------------------------------------------------------ //
    //  C2-5 core: sendStartV2 / sendPerformV2 — single commands          //
    // ------------------------------------------------------------------ //

    @Order(40)
    @Test
    fun `sendStartV2 returns Committed CommandOutcome with transactionId and blockNumber`() = runTest {
        val sessionName = "v2-deal-start-$uuid"
        val roles = mapOf(agentUser1.name to "Buyer", agentUser2.name to "Seller")
        val session = SsmSession(ssmName, sessionName, roles, "Starting v2 test", emptyMap())
        val commandId = "cmd-start-$uuid"

        val commands = listOf(
            SsmStartCommandV2(
                commandId = commandId,
                session = session,
                chaincodeUri = chaincodeUri,
                signerName = signerAdmin.name,
            )
        )
        val outcomes: List<CommandOutcome> = tx.sendStartV2(commands)

        assertThat(outcomes).hasSize(1)
        val outcome = outcomes[0]
        assertThat(outcome.outcome).isEqualTo("Committed")
        assertThat(outcome.commandId).isEqualTo(commandId)
        assertThat(outcome.transactionId).isNotNull.isNotEmpty
        assertThat(outcome.blockNumber).isNotNull.isGreaterThan(0L)

        // Verify session was actually started on chain
        val sessionState = query.getSession(chaincodeUri, sessionName)
        assertThat(sessionState).isNotNull
        assertThat(sessionState!!.current).isEqualTo(0)
        assertThat(sessionState.ssm).isEqualTo(ssmName)
    }

    @Order(50)
    @Test
    fun `sendPerformV2 returns Committed CommandOutcome with transactionId and blockNumber`() = runTest {
        // Need a dedicated session for this test (Order 40 session can't be reused easily)
        val sessionName = "v2-deal-perform-$uuid"
        val roles = mapOf(agentUser1.name to "Buyer", agentUser2.name to "Seller")
        val session = SsmSession(ssmName, sessionName, roles, "Perform v2 test", emptyMap())

        // Start session via v1 path first so we can perform on it
        tx.sendStart(chaincodeUri, session, signerAdmin.name)

        val commandId = "cmd-perform-$uuid"
        val context = SsmContext(sessionName, "Selling via v2", 0, emptyMap())
        val commands = listOf(
            SsmPerformCommandV2(
                commandId = commandId,
                action = "Sell",
                context = context,
                chaincodeUri = chaincodeUri,
                signerName = signerUser2.name,
            )
        )
        val outcomes: List<CommandOutcome> = tx.sendPerformV2(commands)

        assertThat(outcomes).hasSize(1)
        val outcome = outcomes[0]
        assertThat(outcome.outcome).isEqualTo("Committed")
        assertThat(outcome.commandId).isEqualTo(commandId)
        assertThat(outcome.transactionId).isNotNull.isNotEmpty
        assertThat(outcome.blockNumber).isNotNull.isGreaterThan(0L)

        // Verify action transitioned the session
        val sessionState = query.getSession(chaincodeUri, sessionName)
        assertThat(sessionState).isNotNull
        assertThat(sessionState!!.current).isEqualTo(1)
    }

    // ------------------------------------------------------------------ //
    //  Batch tests: 2 commands in one /invoke/v2 call                    //
    // ------------------------------------------------------------------ //

    @Order(60)
    @Test
    fun `sendStartV2 with batch of 2 returns 2 Committed outcomes preserving commandIds`() = runTest {
        val sessionName1 = "v2-batch-start-1-$uuid"
        val sessionName2 = "v2-batch-start-2-$uuid"
        val roles = mapOf(agentUser1.name to "Buyer", agentUser2.name to "Seller")

        val commandId1 = "cmd-batch-start-1-$uuid"
        val commandId2 = "cmd-batch-start-2-$uuid"

        val commands = listOf(
            SsmStartCommandV2(
                commandId = commandId1,
                session = SsmSession(ssmName, sessionName1, roles, "Batch start 1", emptyMap()),
                chaincodeUri = chaincodeUri,
                signerName = signerAdmin.name,
            ),
            SsmStartCommandV2(
                commandId = commandId2,
                session = SsmSession(ssmName, sessionName2, roles, "Batch start 2", emptyMap()),
                chaincodeUri = chaincodeUri,
                signerName = signerAdmin.name,
            )
        )
        val outcomes: List<CommandOutcome> = tx.sendStartV2(commands)

        assertThat(outcomes).hasSize(2)
        outcomes.forEachIndexed { index, outcome ->
            assertThat(outcome.outcome).isEqualTo("Committed")
            assertThat(outcome.commandId).isEqualTo(commands[index].commandId)
            assertThat(outcome.transactionId).isNotNull.isNotEmpty
            assertThat(outcome.blockNumber).isNotNull.isGreaterThan(0L)
        }
    }

    @Order(70)
    @Test
    fun `sendPerformV2 with batch of 2 returns 2 Committed outcomes preserving commandIds`() = runTest {
        val sessionName1 = "v2-batch-perform-1-$uuid"
        val sessionName2 = "v2-batch-perform-2-$uuid"
        val roles = mapOf(agentUser1.name to "Buyer", agentUser2.name to "Seller")

        // Start both sessions via v1 path
        val session1 = SsmSession(ssmName, sessionName1, roles, "Batch perform 1", emptyMap())
        val session2 = SsmSession(ssmName, sessionName2, roles, "Batch perform 2", emptyMap())
        tx.sendStart(chaincodeUri, session1, signerAdmin.name)
        tx.sendStart(chaincodeUri, session2, signerAdmin.name)

        val commandId1 = "cmd-batch-perform-1-$uuid"
        val commandId2 = "cmd-batch-perform-2-$uuid"

        val commands = listOf(
            SsmPerformCommandV2(
                commandId = commandId1,
                action = "Sell",
                context = SsmContext(sessionName1, "Selling batch 1 via v2", 0, emptyMap()),
                chaincodeUri = chaincodeUri,
                signerName = signerUser2.name,
            ),
            SsmPerformCommandV2(
                commandId = commandId2,
                action = "Sell",
                context = SsmContext(sessionName2, "Selling batch 2 via v2", 0, emptyMap()),
                chaincodeUri = chaincodeUri,
                signerName = signerUser2.name,
            )
        )
        val outcomes: List<CommandOutcome> = tx.sendPerformV2(commands)

        assertThat(outcomes).hasSize(2)
        outcomes.forEachIndexed { index, outcome ->
            assertThat(outcome.outcome).isEqualTo("Committed")
            assertThat(outcome.commandId).isEqualTo(commands[index].commandId)
            assertThat(outcome.transactionId).isNotNull.isNotEmpty
            assertThat(outcome.blockNumber).isNotNull.isGreaterThan(0L)
        }

        // Verify both sessions transitioned
        val state1 = query.getSession(chaincodeUri, sessionName1)
        val state2 = query.getSession(chaincodeUri, sessionName2)
        assertThat(state1!!.current).isEqualTo(1)
        assertThat(state2!!.current).isEqualTo(1)
    }
}
