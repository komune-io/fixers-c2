package s2.spring.automate.ssm

import f2.dsl.fnc.invoke
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import s2.automate.core.persist.AutomatePersister
import s2.dsl.automate.Evt
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.dsl.automate.ssm.toSsm
import s2.spring.automate.S2ConfigurerAdapter
import s2.spring.automate.executor.S2AutomateExecutorSpring
import s2.spring.automate.ssm.persister.SsmAutomatePersister
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.tx.dsl.features.ssm.SsmInitCommand
import ssm.tx.dsl.features.ssm.SsmTxInitFunction
import tools.jackson.databind.ObjectMapper

abstract class S2SsmConfigurerAdapter<STATE, ID, ENTITY, AGGREGATE> :
	S2ConfigurerAdapter<STATE, ID, ENTITY, AGGREGATE>() where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>,
AGGREGATE : S2AutomateExecutorSpring<STATE, ID, ENTITY> {

	@Autowired
	lateinit var ssmTxInitFunction: SsmTxInitFunction

	@Autowired
	lateinit var ssmSessionStartFunction: SsmTxSessionStartFunction

	@Autowired
	lateinit var ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction

	@Autowired
	lateinit var ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction

	@Autowired
	lateinit var objectMapper: ObjectMapper

	override fun aggregateRepository(): AutomatePersister<STATE, ID, ENTITY, Evt, S2Automate> = runBlocking {
		val automate = automate()
		val signer = signerAgent()
		val chaincodeUri = chaincodeUri()
		SsmAutomatePersister<STATE, ID, ENTITY, Evt>(
			ssmSessionStartFunction = ssmSessionStartFunction,
			ssmSessionPerformActionFunction = ssmSessionPerformActionFunction,
			objectMapper = objectMapper,
			ssmGetSessionLogsQueryFunction = ssmGetSessionLogsQueryFunction,
			entityType = entityType(),
			chaincodeUri = chaincodeUri,
			agentSigner = signer,
			permissive = permissive,
			batch = batchParams,
			timestampProvider = ::businessTimestampOf
		).also {
			ssmTxInitFunction.invoke(
				SsmInitCommand(
					signerName = signer.name,
					ssm = automate.toSsm(permissive),
					agent = signer,
					chaincodeUri = chaincodeUri
				)
			)
		}
	}

	abstract fun entityType(): Class<ENTITY>
	abstract fun chaincodeUri(): ChaincodeUri
	abstract fun signerAgent(): Agent

	/**
	 * Optional per-transition business timestamp (epoch millis) resolved from the S2 command
	 * (`S2InitCommand` on start, `S2Command` on perform). Override to stamp the Fabric tx envelope
	 * with a historical date instead of wall-clock. Default: null (wall-clock).
	 */
	open fun businessTimestampOf(msg: Any): Long? = null

	open var permissive = false
}
