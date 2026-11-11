package s2.spring.sourcing.ssm

import f2.dsl.fnc.invoke
import f2.dsl.fnc.operators.batch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.asBatch
import s2.dsl.automate.Evt
import s2.dsl.automate.S2Automate
import s2.dsl.automate.model.WithS2Id
import s2.sourcing.dsl.event.EventRepository

class EventPersisterSsm<EVENT, ID : Any>(
    s2Automate: S2Automate,
    private val batchParams: S2BatchProperties,
    private val functions: SsmSourcingFunctions,
    target: SsmSourcingTarget,
    private val format: SsmEventFormat<EVENT>,
    isVersioning: Boolean = false,
) : EventRepository<EVENT, ID> where
EVENT: Evt,
EVENT: WithS2Id<ID>
{

    private val sessions = SsmEventSessions<EVENT, ID>(
        s2Automate = s2Automate,
        functions = functions,
        chaincodeUri = target.chaincodeUri,
        isVersioning = isVersioning,
    )
    private val commands = SsmEventCommands(s2Automate = s2Automate, target = target, format = format)

    override fun load(id: ID): Flow<EVENT> = flow {
        emitAll(format.decode(sessions.logsOf(listOf(sessions.nameOf(id))).flatMap { it.logs }))
    }

    override fun loadAll(): Flow<EVENT> = flow {
        val logs = sessions.names()
            .chunked(batchParams.size)
            .flatMap { sessionNames -> sessions.logsOf(sessionNames).flatMap { it.logs } }
        emitAll(format.decode(logs))
    }

    override fun persist(events: Flow<EVENT>): Flow<EVENT> {
        return events.batch(batchParams.asBatch()) { chunkedEvents: List<EVENT> ->
            checkDuplication(chunkedEvents)

            val bySessionName = chunkedEvents.associateBy { sessions.nameOfEvent(it) }
            sessions.logsOf(bySessionName.keys).mapNotNull { session ->
                bySessionName[session.sessionName]?.let { event ->
                    ExecutableAction(event = event, iteration = session.logs.maxOfOrNull { it.state.iteration })
                }
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
        val sessionName = sessions.nameOfEvent(event)
        val iteration = sessions.iterationOf(sessionName)
        if(iteration == null) {
            val _ = functions.ssmSessionStartFunction.invoke(
                commands.start(event = event, sessionName = sessionName)
            )
        } else {
            val _ = functions.ssmSessionPerformActionFunction.invoke(
                commands.perform(event = event, sessionName = sessionName, iteration = iteration)
            )
        }
        return event
    }

    private suspend fun Flow<ExecutableAction<EVENT>>.startSessions() {
        val toStart = map { executable ->
            commands.start(event = executable.event, sessionName = sessions.nameOfEvent(executable.event))
        }
        functions.ssmSessionStartFunction.invoke(toStart).collect()
    }

    private suspend fun Flow<ExecutableAction<EVENT>>.performActions() {
        val toPerform = map { executable ->
            commands.perform(
                event = executable.event,
                sessionName = sessions.nameOfEvent(executable.event),
                iteration = executable.iteration ?: 0,
            )
        }
        functions.ssmSessionPerformActionFunction.invoke(toPerform).collect()
    }
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
