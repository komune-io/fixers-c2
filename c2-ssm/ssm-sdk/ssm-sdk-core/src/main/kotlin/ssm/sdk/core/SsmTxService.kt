package ssm.sdk.core

import f2.dsl.fnc.operators.batch
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.toBatch
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.sdk.core.command.SsmCreateCommand
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.core.command.UserRegisterCommand
import ssm.sdk.core.invoke.command.CreateCmd
import ssm.sdk.core.invoke.command.PerformCmd
import ssm.sdk.core.invoke.command.RegisterCmd
import ssm.sdk.core.invoke.command.StartCmd
import ssm.sdk.dsl.InvokeReturn
import ssm.sdk.dsl.SsmCmd

@Suppress("TooManyFunctions")
class SsmTxService(
    private val ssmService: SsmService,
    private val batch: SsmBatchProperties,
) {
	private val logger = LoggerFactory.getLogger(SsmTxService::class.java)


	fun sendRegisterUser(
		commands: Flow<UserRegisterCommand>
	): Flow<InvokeReturn>
			= commands.batch(batch.toBatch(), ::sendRegisterUser)

	fun sendCreate(commands: Flow<SsmCreateCommand>): Flow<InvokeReturn>
			= commands.batch(batch.toBatch(), ::sendCreate)

	fun sendStart(commands: Flow<SsmStartCommand>): Flow<InvokeReturn>
			= commands.batch(batch.toBatch(), ::sendStart)

	fun sendPerform(commands: Flow<SsmPerformCommand>): Flow<InvokeReturn>
			= commands.batch(batch.toBatch(), ::sendPerform)

	suspend fun sendRegisterUser(
		commands: List<UserRegisterCommand>
	): List<InvokeReturn> {
		logger.info("Register ${commands.size} user(s)")
		return ssmService.signsAndSend {
			commands.map { command ->
				registerUser(command.agent, command.chaincodeUri, command.signerName)
			}
		}
	}

	suspend fun sendCreate(commands: List<SsmCreateCommand>): List<InvokeReturn> {
		logger.info("Create ${commands.size} ssm(s)")
		return ssmService.signssAndSend {
			commands.map { command ->
				create(command.ssm, command.chaincodeUri, command.signerName)
			}
		}
	}

	suspend fun sendStart(commands: List<SsmStartCommand>): List<InvokeReturn> {
		logger.info("Start ${commands.size} session(s)")
		return ssmService.signssAndSend {
			commands.map { command ->
				start(command.session, command.chaincodeUri, command.signerName)
			}

		}
	}

	suspend fun sendPerform(commands: List<SsmPerformCommand>): List<InvokeReturn> {
		logger.info("Perform ${commands.size} action(s)")
		return ssmService.signsAndSend {
			commands.map { command ->
				perform(command.action, command.context, command.chaincodeUri, command.signerName)
			}
		}
	}


	suspend fun sendRegisterUser(chaincodeUri: ChaincodeUri, agent: Agent, signerName: AgentName): InvokeReturn {
		return sendRegisterUser(
			listOf(
				UserRegisterCommand(agent=agent, chaincodeUri=chaincodeUri, signerName=signerName)
			)
		).first()
	}

	suspend fun sendCreate(chaincodeUri: ChaincodeUri, ssm: Ssm, signerName: AgentName): InvokeReturn {
		return sendCreate(
			listOf(
				SsmCreateCommand(ssm=ssm, chaincodeUri=chaincodeUri, signerName=signerName)
			)
		).first()
	}

	suspend fun sendStart(chaincodeUri: ChaincodeUri, session: SsmSession, signerName: AgentName): InvokeReturn {
		return sendStart(
			listOf(
				SsmStartCommand(chaincodeUri=chaincodeUri, session=session, signerName=signerName)
			)
		).first()
	}

	suspend fun sendPerform(
		chaincodeUri: ChaincodeUri, action: String, context: SsmContext, signerName: AgentName
	): InvokeReturn {
		return ssmService.signAndSend {
			perform(action, context, chaincodeUri, signerName)
		}
	}


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
		logger.debug(
			"Start session[${session.session}] ssm[${session.ssm}] with signer[$signerName]"
		)
		val cmd = StartCmd(session)
		return cmd.commandToSign(chaincodeUri, signerName)
	}

	private fun perform(action: String, context: SsmContext, chaincodeUri: ChaincodeUri, agentName: AgentName): SsmCmd {
		logger.debug(
			"Perform action[${action}] session[${context.session}] with signer[$agentName]"
		)
		val cmd = PerformCmd(action, context)
		return cmd.commandToSign(chaincodeUri, agentName)
	}

}
