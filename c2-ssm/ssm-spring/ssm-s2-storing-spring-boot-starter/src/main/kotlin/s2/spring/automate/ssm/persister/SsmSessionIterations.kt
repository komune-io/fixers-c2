package s2.spring.automate.ssm.persister

import f2.dsl.fnc.operators.batchFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.asBatch
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2Iteration
import s2.dsl.automate.model.WithS2State
import s2.dsl.automate.s2error
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult

/**
 * Resolves the current on-chain iteration of each transition's session: from the entity when it
 * carries it ([WithS2Iteration]), otherwise from the chain's session logs, batched, where a missing
 * or log-less session becomes a [GetSessionResult.failure].
 */
internal class SsmSessionIterations<STATE, ID : Any, ENTITY, EVENT>(
    entityType: Class<ENTITY>,
    private val batch: S2BatchProperties,
    private val sessionLogs: (Flow<GetAutomateSessionQuery>) -> Flow<SsmGetSessionLogsQueryResult>,
) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> {

    private val isCarriedByEntity = WithS2Iteration::class.java.isAssignableFrom(entityType)

    fun iterationsOf(
        query: Flow<GetSessionQuery<STATE, ID, ENTITY, EVENT>>,
    ): Flow<GetSessionResult<STATE, ID, ENTITY, EVENT>> =
        if (isCarriedByEntity) fromEntity(query) else fromChain(query)

    private fun fromEntity(
        query: Flow<GetSessionQuery<STATE, ID, ENTITY, EVENT>>
    ): Flow<GetSessionResult<STATE, ID, ENTITY, EVENT>> = query.map { sessionQuery ->
        val entity = sessionQuery.transitionContext.entity as WithS2Iteration
        GetSessionResult(
            transitionContext = sessionQuery.transitionContext,
            sessionId = sessionQuery.sessionId,
            iteration = entity.s2Iteration(),
        )
    }

    private fun fromChain(
        query: Flow<GetSessionQuery<STATE, ID, ENTITY, EVENT>>
    ): Flow<GetSessionResult<STATE, ID, ENTITY, EVENT>> = query.batchFlow(batch.asBatch()) { list ->
        val bySession = list.associateBy { it.sessionId }
        val logsQueries = list.map { sessionQuery ->
            GetAutomateSessionQuery(
                automateContext = sessionQuery.transitionContext.automateContext,
                sessionId = sessionQuery.sessionId,
            )
        }
        val sessions = sessionLogs(logsQueries.asFlow()).toList()
        val foundIds = sessions.map { it.sessionName }.toSet()
        val happy = sessions.map { session ->
            val context = bySession.getValue(session.sessionName)
            val iteration = session.logs.maxOfOrNull { it.state.iteration } ?: -1
            if (iteration < 0) {
                GetSessionResult(
                    transitionContext = context.transitionContext,
                    sessionId = context.sessionId,
                    iteration = 0,
                    failure = PersistOutcome.Rejected(
                        msgId = lookupMsgId(context),
                        error = s2error(
                            code = "NO_LOGS",
                            description = "No logs for session ${session.sessionName}",
                        ),
                    ),
                )
            } else {
                GetSessionResult(
                    transitionContext = context.transitionContext,
                    sessionId = context.sessionId,
                    iteration = iteration,
                )
            }
        }
        val missing = list.filterNot { it.sessionId in foundIds }.map { miss ->
            GetSessionResult(
                transitionContext = miss.transitionContext,
                sessionId = miss.sessionId,
                iteration = 0,
                failure = PersistOutcome.Rejected(
                    msgId = lookupMsgId(miss),
                    error = s2error(
                        code = "SESSION_NOT_FOUND",
                        description = "Session ${miss.sessionId} not on chaincode",
                    ),
                ),
            )
        }
        (happy + missing).asFlow()
    }

    private fun lookupMsgId(query: GetSessionQuery<STATE, ID, ENTITY, EVENT>): String = "${query.sessionId}:lookup"
}
