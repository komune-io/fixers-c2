package s2.spring.sourcing.ssm

import f2.dsl.fnc.invoke
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import s2.dsl.automate.Evt
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.dsl.automate.ssm.toSsm
import s2.sourcing.dsl.event.EventRepository
import s2.sourcing.dsl.snap.SnapRepository
import s2.sourcing.dsl.view.View
import s2.spring.automate.sourcing.S2AutomateDeciderSpring
import s2.spring.automate.sourcing.S2AutomateDeciderSpringAdapter
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.tx.dsl.features.ssm.SsmInitCommand
import ssm.tx.dsl.features.ssm.SsmTxInitFunction

abstract class S2SourcingSsmAdapter<ENTITY, STATE, EVENT, ID, EXECUTOR>(
	executor: EXECUTOR,
	view: View<EVENT, ENTITY>,
	snapRepository: SnapRepository<ENTITY, ID>? = null
): S2AutomateDeciderSpringAdapter<ENTITY, STATE, EVENT, ID, EXECUTOR>(executor, view, snapRepository) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>,
EVENT: WithS2Id<ID>,
EVENT: Evt,
EXECUTOR : S2AutomateDeciderSpring<ENTITY, STATE, EVENT, ID> {

	@Autowired
	lateinit var ssmTxInitFunction: SsmTxInitFunction

	@Autowired
	lateinit var ssmSessionStartFunction: SsmTxSessionStartFunction

	@Autowired
	lateinit var ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction

	@Autowired
	lateinit var dataSsmSessionGetQueryFunction: DataSsmSessionGetQueryFunction

	@Autowired
	lateinit var dataSsmSessionListQueryFunction: DataSsmSessionListQueryFunction

	@Autowired
	lateinit var ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction

	override fun eventStore(): EventRepository<EVENT, ID> = runBlocking {
		val automate = automate()
		val signer = signerAgent()
		val chaincodeUri = chaincodeUri()
		EventPersisterSsm(
			s2Automate = automate,
			eventType = entityType(),
			batchParams = batchParams,
			ssmSessionStartFunction = ssmSessionStartFunction,
			ssmSessionPerformActionFunction = ssmSessionPerformActionFunction,
			dataSsmSessionGetQueryFunction = dataSsmSessionGetQueryFunction,
			ssmGetSessionLogsQueryFunction = ssmGetSessionLogsQueryFunction,
			dataSsmSessionListQueryFunction = dataSsmSessionListQueryFunction,
			chaincodeUri = chaincodeUri,
			agentSigner = signer,
			json = json(),
			versioning = versioning,
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

	open fun json(): Json = Json

	abstract fun chaincodeUri(): ChaincodeUri
	abstract fun signerAgent(): Agent
	open var permissive = false
	open var versioning = false
}
