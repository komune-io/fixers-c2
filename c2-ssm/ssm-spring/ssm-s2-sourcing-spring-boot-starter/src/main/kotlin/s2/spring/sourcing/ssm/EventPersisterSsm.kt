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
import ssm.data.dsl.features.query.DataSsmSessionGetQuery
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQuery
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.tx.dsl.features.ssm.SsmSessionPerformActionCommand
import ssm.tx.dsl.features.ssm.SsmSessionPerformActionResult
import ssm.tx.dsl.features.ssm.SsmSessionStartCommand
import ssm.tx.dsl.features.ssm.SsmSessionStartResult
import ssm.tx.dsl.features.ssm.SsmTxSessionPerformActionFunction
import ssm.tx.dsl.features.ssm.SsmTxSessionStartFunction

class EventPersisterSsm<EVENT, ID>(
	private val s2Automate: S2Automate,
	private val eventType: KClass<EVENT>,
	private val batchParams: S2BatchProperties,
) : EventRepository<EVENT, ID> where
EVENT: Evt,
EVENT: WithS2Id<ID>
{

	internal lateinit var ssmSessionStartFunction: SsmTxSessionStartFunction
	internal lateinit var ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction

	internal lateinit var dataSsmSessionGetQueryFunction: DataSsmSessionGetQueryFunction
	internal lateinit var ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction
	internal lateinit var dataSsmSessionListQueryFunction: DataSsmSessionListQueryFunction

	internal lateinit var chaincodeUri: ChaincodeUri
	internal lateinit var agentSigner: Agent
	internal lateinit var json: Json
	internal var versioning: Boolean = false

	override suspend fun load(id: ID): Flow<EVENT> {
		return getSessionLogs(listOf(id.toString()))
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
			}.map { (session, event) ->
				val iteration = session.logs.maxOfOrNull { it.state.iteration }
				ExecutableAction(event!!, iteration)
			}.groupBy {
				it.action
			}.flatMap { (action, eventsByAction) ->
				when(action) {
					Action.CREATE -> eventsByAction.asFlow().initFlow().collect()
					Action.UPDATE -> eventsByAction.asFlow().updateFlow().collect()
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

	override suspend fun createTable() {}

	override suspend fun persist(event: EVENT): EVENT {
		val sessionName = buildSessionName(event)
		val iteration = getIteration(sessionName)
		val action = event::class.simpleName!!
		if(iteration == null) {
			init(event)
		} else {
			@OptIn(InternalSerializationApi::class)
			val public = json.encodeToString(eventType.serializer(), event)
			val context = SsmSessionPerformActionCommand(
				action = action,
				context = SsmContext(
					session = sessionName,
					public = public,
					private = mapOf(),
					iteration = iteration,
				),
				signerName = agentSigner.name,
				chaincodeUri = chaincodeUri
			)
			ssmSessionPerformActionFunction.invoke(context)
		}
		return event
	}

	private suspend fun Flow<ExecutableAction<EVENT>>.initFlow( ): Flow<SsmSessionStartResult> = map { event ->
		val sessionName = buildSessionName(event.event)
		@OptIn(InternalSerializationApi::class)
		val public = json.encodeToString(eventType.serializer(), event.event)
		SsmSessionStartCommand(
			session = SsmSession(
				ssm = s2Automate.name,
				session = sessionName,
				roles = mapOf(agentSigner.name to s2Automate.transitions[0].role.name),
				public = public,
				private = mapOf()
			),
			signerName = agentSigner.name,
			chaincodeUri = chaincodeUri
		)
	}.let {
		ssmSessionStartFunction.invoke(it)
	}

	private suspend fun Flow<ExecutableAction<EVENT>>.updateFlow(): Flow<SsmSessionPerformActionResult> = map { event ->
		val sessionName = buildSessionName(event.event)
		val action = event.event::class.simpleName!!
		@OptIn(InternalSerializationApi::class)
		SsmSessionPerformActionCommand(
			action = action,
			context = SsmContext(
				session = sessionName,
				public = json.encodeToString(eventType.serializer(), event.event),
				private = mapOf(),
				iteration = event.iteration ?: 0,
			),
			signerName = agentSigner.name,
			chaincodeUri = chaincodeUri
		)
	}.let { toUpdated ->
		ssmSessionPerformActionFunction.invoke(toUpdated)
	}

	private suspend fun init(event: EVENT): EVENT {
		@OptIn(InternalSerializationApi::class)
		val ssmStart = SsmSessionStartCommand(
			session = SsmSession(
				ssm = s2Automate.name,
				session = buildSessionName(event),
				roles = mapOf(agentSigner.name to s2Automate.transitions[0].role.name),
				public = json.encodeToString(eventType.serializer(), event),
				private = mapOf()
			),
			signerName = agentSigner.name,
			chaincodeUri = chaincodeUri
		)
		ssmSessionStartFunction.invoke(ssmStart)
		return event
	}

	private fun buildSessionName(event: EVENT): String {
		return if(versioning) {
			return "${s2Automate.name}-${event.s2Id()}"
		} else {
			event.s2Id().toString()
		}
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
	) = sessionNames.map { sessionName ->
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
	): List<SsmSessionStateLog> = sessionIds.map { sessionId ->
		SsmGetSessionLogsQuery(
			sessionName = sessionId,
			chaincodeUri = chaincodeUri,
			ssmName = s2Automate.name
		)
	}.let{
		ssmGetSessionLogsQueryFunction.invoke(it.asFlow())
	}.toList().flatMap { it.logs }

	private suspend fun listSessions() = DataSsmSessionListQuery(
		ssmUri = chaincodeUri.toSsmUri(s2Automate.name)
	).invokeWith(dataSsmSessionListQueryFunction)

	@OptIn(InternalSerializationApi::class)
	private fun List<SsmSessionStateLog>.toEvents(): Flow<EVENT> = sortedBy { it.state.iteration }.map {
		json.decodeFromString(eventType.serializer(), it.state.public as String)
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
