package ssm.sdk.core

import f2.dsl.fnc.operators.batch
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.toBatch
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.core.command.SsmCreateCommand
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.core.command.UserRegisterCommand
import ssm.sdk.core.invoke.command.CreateCmd
import ssm.sdk.core.invoke.command.PerformCmd
import ssm.sdk.core.invoke.command.RegisterCmd
import ssm.sdk.core.invoke.command.StartCmd
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.SsmCmd
import java.util.UUID

@Suppress("TooManyFunctions")
class SsmTxService(
    private val ssmService: SsmService,
    private val batch: SsmBatchProperties,
) {
	private val logger = LoggerFactory.getLogger(SsmTxService::class.java)

	fun sendRegisterUser(commands: Flow<UserRegisterCommand>): Flow<CommandOutcome> =
		commands.batch(batch.toBatch(), ::sendRegisterUser)

	fun sendCreate(commands: Flow<SsmCreateCommand>): Flow<CommandOutcome> =
		commands.batch(batch.toBatch(), ::sendCreate)

	fun sendStart(commands: Flow<SsmStartCommand>): Flow<CommandOutcome> =
		commands.batch(batch.toBatch(), ::sendStart)

	fun sendPerform(commands: Flow<SsmPerformCommand>): Flow<CommandOutcome> =
		commands.batch(batch.toBatch(), ::sendPerform)

	suspend fun sendRegisterUser(commands: List<UserRegisterCommand>): List<CommandOutcome> {
		logger.info("Register ${commands.size} user(s)")
		val cmds = commands.map { registerUser(it.agent, it.chaincodeUri, it.signerName) }
		val ids = commands.map { UUID.randomUUID().toString() }
		return ssmService.invokeAll(cmds, ids)
	}

	suspend fun sendCreate(commands: List<SsmCreateCommand>): List<CommandOutcome> {
		logger.info("Create ${commands.size} ssm(s)")
		val cmds = commands.map { create(it.ssm, it.chaincodeUri, it.signerName) }
		val ids = commands.map { UUID.randomUUID().toString() }
		return ssmService.invokeAll(cmds, ids)
	}

	suspend fun sendStart(commands: List<SsmStartCommand>): List<CommandOutcome> {
		logger.info("Start ${commands.size} session(s)")
		val cmds = commands.map { start(it.session, it.chaincodeUri, it.signerName) }
		val ids = commands.map { it.msgId }
		return ssmService.invokeAll(cmds, ids)
	}

	suspend fun sendPerform(commands: List<SsmPerformCommand>): List<CommandOutcome> {
		logger.info("Perform ${commands.size} action(s)")
		val cmds = commands.map { perform(it.action, it.context, it.chaincodeUri, it.signerName) }
		val ids = commands.map { it.msgId }
		return ssmService.invokeAll(cmds, ids)
	}

	suspend fun sendRegisterUser(chaincodeUri: ChaincodeUri, agent: Agent, signerName: AgentName): CommandOutcome =
		sendRegisterUser(listOf(
			UserRegisterCommand(agent = agent, chaincodeUri = chaincodeUri, signerName = signerName)
		)).first()

	suspend fun sendCreate(chaincodeUri: ChaincodeUri, ssm: Ssm, signerName: AgentName): CommandOutcome =
		sendCreate(listOf(SsmCreateCommand(ssm = ssm, chaincodeUri = chaincodeUri, signerName = signerName))).first()

	suspend fun sendPerform(
        chaincodeUri: ChaincodeUri, action: String, context: SsmContext, signerName: AgentName
	): CommandOutcome = sendPerform(listOf(SsmPerformCommand(
		msgId = UUID.randomUUID().toString(),
		action = action,
		context = context,
		chaincodeUri = chaincodeUri,
		signerName = signerName,
	))).first()

	suspend fun sendStart(chaincodeUri: ChaincodeUri, session: SsmSession, signerName: AgentName): CommandOutcome =
		sendStart(listOf(SsmStartCommand(
			msgId = UUID.randomUUID().toString(),
			chaincodeUri = chaincodeUri,
			signerName = signerName,
			session = session,
		))).first()

	private fun registerUser(agent: Agent, chaincodeUri: ChaincodeUri, signerName: AgentName): SsmCmd {
		logger.debug("Register user[${agent.name}] with signer[$signerName]")
		val cmd = RegisterCmd(agent)
		return cmd.commandToSign(chaincodeUri, signerName)
	}

	private fun create(ssm: Ssm, chaincodeUri: ChaincodeUri, signerName: AgentName): SsmCmd {
		logger.debug("Create ssm[${ssm.name}] with signer[$signerName]")
		val cmd = CreateCmd(ssm)
		return cmd.commandToSign(chaincodeUri, signerName)
	}

	private fun start(session: SsmSession, chaincodeUri: ChaincodeUri, signerName: AgentName): SsmCmd {
		logger.debug("Start session[${session.session}] ssm[${session.ssm}] with signer[$signerName]")
		val cmd = StartCmd(session)
		return cmd.commandToSign(chaincodeUri, signerName)
	}

	private fun perform(action: String, context: SsmContext, chaincodeUri: ChaincodeUri, agentName: AgentName): SsmCmd {
		logger.debug("Perform action[${action}] session[${context.session}] with signer[$agentName]")
		val cmd = PerformCmd(action, context)
		return cmd.commandToSign(chaincodeUri, agentName)
	}
}
