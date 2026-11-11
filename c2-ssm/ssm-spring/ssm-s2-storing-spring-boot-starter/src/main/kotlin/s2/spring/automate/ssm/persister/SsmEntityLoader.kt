package s2.spring.automate.ssm.persister

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import s2.automate.core.context.AutomateContext
import s2.automate.core.persist.LoadOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.model.WithS2Iteration
import s2.dsl.automate.s2error
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import tools.jackson.databind.ObjectMapper

/**
 * Loads entities from their sessions' last log, one [LoadOutcome] per id, so a single bad
 * session in a batch surfaces as one [LoadOutcome.Rejected] instead of aborting the whole chunk.
 *
 * Failure modes:
 *  - chaincode-query throws (network / gRPC) → [LoadOutcome.Transient] for
 *    every id (we can't classify per-id when the query itself failed);
 *  - session not in the result set (chain has no such id) →
 *    [LoadOutcome.Rejected] with `SESSION_NOT_FOUND`;
 *  - session returned but logs empty → [LoadOutcome.Rejected] with
 *    `SESSION_NOT_INITIALIZED`;
 *  - the last log carries no state, or it cannot be read as the entity → [LoadOutcome.Rejected]
 *    with `DESERIALIZATION_FAILED` (a bad on-chain write — retry won't fix).
 */
internal class SsmEntityLoader<ID : Any, ENTITY>(
    private val objectMapper: ObjectMapper,
    private val entityType: Class<ENTITY>,
    private val sessionLogs: (Flow<GetAutomateSessionQuery>) -> Flow<SsmGetSessionLogsQueryResult>,
) {

    // Any failure of the chain read is a retryable outcome for every id, never an exception.
    @Suppress("TooGenericExceptionCaught")
    suspend fun load(automateContext: AutomateContext<S2Automate>, ids: List<ID>): List<LoadOutcome<ID, ENTITY>> {
        val sessions = try {
            sessionsByName(automateContext = automateContext, ids = ids)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ids.map { id ->
                LoadOutcome.Transient(
                    id = id,
                    error = s2error(
                        code = "CHAINCODE_QUERY_FAILED",
                        description = e.message ?: e::class.simpleName ?: "<no message>",
                        payload = mapOf("id" to id.toString()),
                        cause = e,
                    ),
                )
            }
        }
        return ids.map { id -> outcomeFor(id = id, session = sessions[id.toString()]) }
    }

    // distinct(): several commands of a batch may target the same entity; the lookup is the same call.
    private suspend fun sessionsByName(
        automateContext: AutomateContext<S2Automate>,
        ids: List<ID>,
    ): Map<String, SsmGetSessionLogsQueryResult> = if (ids.isEmpty()) {
        emptyMap()
    } else {
        val queries = ids.distinct().map { id ->
            GetAutomateSessionQuery(automateContext = automateContext, sessionId = id.toString())
        }
        sessionLogs(queries.asFlow()).toList().associateBy { it.sessionName }
    }

    private fun outcomeFor(id: ID, session: SsmGetSessionLogsQueryResult?): LoadOutcome<ID, ENTITY> {
        val sessionName = id.toString()
        return when {
            session == null -> LoadOutcome.Rejected(
                id = id,
                error = s2error(
                    code = "SESSION_NOT_FOUND",
                    description = "Session $sessionName not on chaincode",
                    payload = mapOf("id" to sessionName),
                ),
            )
            session.logs.isEmpty() -> LoadOutcome.Rejected(
                id = id,
                error = s2error(
                    code = "SESSION_NOT_INITIALIZED",
                    description = "No logs for session $sessionName",
                    payload = mapOf("id" to sessionName),
                ),
            )
            else -> entityFromLastLog(id = id, lastLog = session.logs.maxBy { it.state.iteration })
        }
    }

    // Any failure to read the entity — Jackson's, or a stamped iteration of another type — rejects this id only.
    @Suppress("TooGenericExceptionCaught")
    private fun entityFromLastLog(id: ID, lastLog: SsmSessionStateLog): LoadOutcome<ID, ENTITY> {
        val payload = mapOf("id" to id.toString())
        val public = lastLog.state.public ?: return LoadOutcome.Rejected(
            id = id,
            error = s2error(
                code = "DESERIALIZATION_FAILED",
                description = "No public state in the last log of session $id",
                payload = payload,
            ),
        )
        return try {
            val raw = objectMapper.readValue(public.toString(), entityType)
            // The iteration is already in the fetched logs: carrying it spares persist an iteration query.
            val entity = if (raw is WithS2Iteration) {
                entityType.cast(raw.withS2Iteration(lastLog.state.iteration))
            } else {
                raw
            }
            LoadOutcome.Loaded(id = id, entity = entity)
        } catch (e: CancellationException) {
            // Not dead in a non-suspend function: a mapper or a stamping hook can throw one, and it must go on.
            throw e
        } catch (e: Exception) {
            LoadOutcome.Rejected(
                id = id,
                error = s2error(
                    code = "DESERIALIZATION_FAILED",
                    description = e.message ?: e::class.simpleName ?: "<no message>",
                    payload = payload,
                    cause = e,
                ),
            )
        }
    }
}
