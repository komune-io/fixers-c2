package s2.spring.sourcing.ssm

import f2.dsl.fnc.invokeWith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import s2.dsl.automate.S2Automate
import s2.dsl.automate.model.WithS2Id
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.data.dsl.features.query.DataSsmSessionGetQuery
import ssm.data.dsl.features.query.DataSsmSessionListQuery
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import java.util.UUID

/** Names, looks up and reads the sessions an [EventPersisterSsm] stores its events in. */
internal class SsmEventSessions<EVENT, ID : Any>(
    private val s2Automate: S2Automate,
    private val functions: SsmSourcingFunctions,
    private val chaincodeUri: ChaincodeUri,
    private val isVersioning: Boolean,
) where EVENT : WithS2Id<ID> {

    fun nameOf(id: ID): SessionName = if (isVersioning) "${s2Automate.name}-$id" else id.toString()

    fun nameOfEvent(event: EVENT): SessionName = nameOf(event.s2Id())

    suspend fun iterationOf(sessionName: SessionName): Int? = DataSsmSessionGetQuery(
        sessionName = sessionName,
        ssmUri = chaincodeUri.toSsmUri(s2Automate.name),
    ).invokeWith(functions.dataSsmSessionGetQueryFunction)
        .item
        ?.run { state.details.iteration }

    suspend fun logsOf(sessionNames: Collection<SessionName>): List<SsmGetSessionLogsQueryResult> {
        val queries = sessionNames.map { sessionName ->
            SsmGetSessionLogsQuery(
                sessionName = sessionName,
                chaincodeUri = chaincodeUri,
                ssmName = s2Automate.name,
            )
        }
        return functions.ssmGetSessionLogsQueryFunction.invoke(queries.asFlow()).toList()
    }

    suspend fun names(): List<SessionName> = DataSsmSessionListQuery(
        ssmUri = chaincodeUri.toSsmUri(s2Automate.name),
    ).invokeWith(functions.dataSsmSessionListQueryFunction)
        .items
        .map { it.sessionName }
}

/** Builds the chaincode commands an [EventPersisterSsm] sends for each event. */
internal class SsmEventCommands<EVENT : Any>(
    private val s2Automate: S2Automate,
    private val target: SsmSourcingTarget,
    private val format: SsmEventFormat<EVENT>,
) {

    fun start(event: EVENT, sessionName: SessionName) = SsmStartCommand(
        msgId = UUID.randomUUID().toString(),
        session = SsmSession(
            ssm = s2Automate.name,
            session = sessionName,
            roles = mapOf(target.agentSigner.name to s2Automate.transitions.first().role.name),
            public = format.encode(event),
            private = emptyMap(),
        ),
        signerName = target.agentSigner.name,
        chaincodeUri = target.chaincodeUri,
    )

    fun perform(event: EVENT, sessionName: SessionName, iteration: Int) = SsmPerformCommand(
        msgId = UUID.randomUUID().toString(),
        action = requireNotNull(event::class.simpleName) { "An anonymous class cannot name an SSM action" },
        context = SsmContext(
            session = sessionName,
            public = format.encode(event),
            private = emptyMap(),
            iteration = iteration,
        ),
        signerName = target.agentSigner.name,
        chaincodeUri = target.chaincodeUri,
    )
}

@OptIn(InternalSerializationApi::class)
internal fun <EVENT : Any> SsmEventFormat<EVENT>.encode(event: EVENT): String =
    json.encodeToString(eventType.serializer(), event)

@OptIn(InternalSerializationApi::class)
internal fun <EVENT : Any> SsmEventFormat<EVENT>.decode(logs: List<SsmSessionStateLog>): Flow<EVENT> =
    logs.sortedBy { it.state.iteration }.map { log ->
        val publicState = log.state.public
        val publicData = publicState as? String
            ?: error("Expected state.public to be String but was ${publicState?.run { javaClass.name } ?: "absent"}")
        json.decodeFromString(eventType.serializer(), publicData)
    }.asFlow()
