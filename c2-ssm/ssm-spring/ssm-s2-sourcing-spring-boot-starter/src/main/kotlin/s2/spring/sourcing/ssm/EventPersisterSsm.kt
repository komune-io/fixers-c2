package s2.spring.sourcing.ssm

import f2.dsl.fnc.invoke
import f2.dsl.fnc.invokeWith
import f2.dsl.fnc.operators.batch
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.asBatch
import s2.dsl.automate.Evt
import s2.dsl.automate.S2Automate
import s2.dsl.automate.model.WithS2Id
import s2.sourcing.dsl.event.EventRepository
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQuery
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQuery
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import java.util.UUID

class EventPersisterSsm<EVENT, ID>(
	private val s2Automate: S2Automate,
	private val eventType: KClass<EVENT>,
	private val batchParams: S2BatchProperties,
	private val ssmSessionStartFunction: SsmTxSessionStartFunction,
	private val ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction,
	private val dataSsmSessionGetQueryFunction: DataSsmSessionGetQueryFunction,
	private val ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
	private val dataSsmSessionListQueryFunction: DataSsmSessionListQueryFunction,
	private val chaincodeUri: ChaincodeUri,
	private val agentSigner: Agent,
	private val json: Json,
	private val versioning: Boolean = false,
) : EventRepository<EVENT, ID> where
EVENT: Evt,
EVENT: WithS2Id<ID>
{

	override suspend fun load(id: ID): Flow<EVENT> {
		val sessionName = buildSessionName(id)
		return getSessionLogs(listOf(sessionName))
			.toEvents()
	}

	@Suppress("MagicNumber")
	override suspend fun loadAll(): Flow<EVENT> {
		return listSessions()
			.items.map { it.sessionName }.chunked(batchParams.size).flatMap {
				getSessionLogs(it)
			}.sortedBy {
				it.state.iteration
			}.toEvents()
	}

	override suspend fun persist(events: Flow<EVENT>): Flow<EVENT> {
		return events.batch(batchParams.asBatch()) { chunkedEvents: List<EVENT> ->
			checkDuplication(chunkedEvents)

			val bySessionName = chunkedEvents.associateBy { buildSessionName(it) }
			getSessions(bySessionName.keys).associateWith {
				bySessionName[it.sessionName]
			}.mapNotNull { (session, event) ->
				event ?: return@mapNotNull null
				val iteration = session.logs.maxOfOrNull { it.state.iteration }
				ExecutableAction(event, iteration)
			}.groupBy {
				it.action
			}.flatMap { (action, eventsByAction) ->
				when(action) {
					Action.CREATE -> eventsByAction.asFlow().startSessions()
					Action.UPDATE -> eventsByAction.asFlow().performActions()
				}
				eventsByAction.map { it.event }
			}
		}
	}

	private fun checkDuplication(chunkedEvents: List<EVENT>) {
		val duplicates = chunkedEvents.groupingBy { it.s2Id() }
			.eachCount()
			.filter { (_, count) -> count > 1 }
			.keys
		require(duplicates.isEmpty()) {
			"Duplicate events detected: ${duplicates.joinToString()}, cannot be processed due to SSM limitations."
		}
	}

	override suspend fun createTable() {
		// No-op: SSM has no table to create, sessions are started lazily on first persist.
	}

	override suspend fun persist(event: EVENT): EVENT {
		val sessionName = buildSessionName(event)
		val iteration = getIteration(sessionName)
		if(iteration == null) {
			init(event)
		} else {
			ssmSessionPerformActionFunction.invoke(performCommandFor(event, iteration))
		}
		return event
	}

	private suspend fun Flow<ExecutableAction<EVENT>>.startSessions() {
		map { startCommandFor(it.event) }.let {
			ssmSessionStartFunction.invoke(it)
		}.collect()
	}

	private suspend fun Flow<ExecutableAction<EVENT>>.performActions() {
		map { performCommandFor(it.event, it.iteration ?: 0) }.let { toUpdated ->
			ssmSessionPerformActionFunction.invoke(toUpdated)
		}.collect()
	}

	private suspend fun init(event: EVENT): EVENT {
		ssmSessionStartFunction.invoke(startCommandFor(event))
		return event
	}

	private fun startCommandFor(event: EVENT) = SsmStartCommand(
		msgId = UUID.randomUUID().toString(),
		session = SsmSession(
			ssm = s2Automate.name,
			session = buildSessionName(event),
			roles = mapOf(agentSigner.name to s2Automate.transitions.first().role.name),
			public = encode(event),
			private = mapOf()
		),
		signerName = agentSigner.name,
		chaincodeUri = chaincodeUri
	)

	private fun performCommandFor(event: EVENT, iteration: Int) = SsmPerformCommand(
		msgId = UUID.randomUUID().toString(),
		action = event::class.simpleName!!,
		context = SsmContext(
			session = buildSessionName(event),
			public = encode(event),
			private = mapOf(),
			iteration = iteration,
		),
		signerName = agentSigner.name,
		chaincodeUri = chaincodeUri
	)

	@OptIn(InternalSerializationApi::class)
	private fun encode(event: EVENT): String = json.encodeToString(eventType.serializer(), event)

	private fun buildSessionName(id: ID): String {
		return if(versioning) {
			"${s2Automate.name}-$id"
		} else {
			id.toString()
		}
	}

	private fun buildSessionName(event: EVENT): String {
		return buildSessionName(event.s2Id())
	}

	private suspend fun getIteration(sessionId: SessionName): Int? {
		return getSession(sessionId)
			.item?.state?.details?.iteration
	}

	private suspend fun getSession(
		sessionId: SessionName,
	) = DataSsmSessionGetQuery(
		sessionName = sessionId,
		ssmUri = chaincodeUri.toSsmUri(s2Automate.name)
	).invokeWith(dataSsmSessionGetQueryFunction)

	private suspend fun getSessions(
		sessionNames: Collection<SessionName>,
	): List<SsmGetSessionLogsQueryResult> = sessionNames.map { sessionName ->
		SsmGetSessionLogsQuery(
			sessionName = sessionName,
			chaincodeUri = chaincodeUri,
			ssmName = s2Automate.name,
		)
	}.let {
		ssmGetSessionLogsQueryFunction.invoke(it.asFlow())
	}.toList()

	private suspend fun getSessionLogs(
		sessionIds: List<SessionName>,
	): List<SsmSessionStateLog> = getSessions(sessionIds).flatMap { it.logs }

	private suspend fun listSessions() = DataSsmSessionListQuery(
		ssmUri = chaincodeUri.toSsmUri(s2Automate.name)
	).invokeWith(dataSsmSessionListQueryFunction)

	@OptIn(InternalSerializationApi::class)
	private fun List<SsmSessionStateLog>.toEvents(): Flow<EVENT> = sortedBy { it.state.iteration }.map {
		val publicData = it.state.public as? String
			?: throw IllegalStateException("Expected state.public to be String but was ${it.state.public?.javaClass}")
		json.decodeFromString(eventType.serializer(), publicData)
	}.asFlow()
}

enum class Action {
	CREATE, UPDATE
}

data class ExecutableAction<EVENT>(
	val event: EVENT,
	val iteration: Int?
) {
	val action: Action = if(iteration == null) Action.CREATE else Action.UPDATE
}
