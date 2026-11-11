package s2.spring.automate.ssm.persister

import s2.automate.core.context.InitTransitionAppliedContext
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import tools.jackson.databind.ObjectMapper

/** Builds the chaincode commands an [SsmAutomatePersister] sends for each transition. */
internal class SsmCommandFactory<STATE, ID : Any, ENTITY, EVENT>(
    private val target: SsmSigningTarget,
    private val objectMapper: ObjectMapper,
    private val timestampProvider: (Any) -> Long?,
) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> {

    /** Builds the session-start command for an init transition. msgId scheme `start:<id>` is load-bearing. */
    fun start(ctx: InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>): SsmStartCommand {
        val entity = ctx.entity
        val automate = ctx.automateContext.automate
        return SsmStartCommand(
            msgId = "start:${entity.s2Id()}",
            session = SsmSession(
                ssm = automate.name,
                session = entity.s2Id().toString(),
                roles = mapOf(target.agentSigner.name to automate.transitions.first().role.name),
                public = objectMapper.writeValueAsString(entity),
                private = emptyMap(),
            ),
            signerName = target.agentSigner.name,
            chaincodeUri = target.chaincodeUri,
            timestamp = timestampProvider(ctx.msg),
        )
    }

    /** Builds the perform command for a transition. msgId scheme `<sessionId>:<iteration+1>` is load-bearing. */
    fun perform(result: GetSessionResult<STATE, ID, ENTITY, EVENT>): SsmPerformCommand {
        val entity = result.transitionContext.entity
        val isResultTheAction = result.transitionContext.automateContext.automate.withResultAsAction
        val action = result.transitionContext.event?.takeIf { isResultTheAction } ?: result.transitionContext.msg
        return SsmPerformCommand(
            msgId = "${result.sessionId}:${result.iteration + 1}",
            action = requireNotNull(action::class.simpleName) { "An anonymous class cannot name an SSM action" },
            context = SsmContext(
                session = entity.s2Id().toString(),
                public = objectMapper.writeValueAsString(entity),
                private = emptyMap(),
                iteration = result.iteration,
            ),
            signerName = target.agentSigner.name,
            chaincodeUri = target.chaincodeUri,
            timestamp = timestampProvider(result.transitionContext.msg),
        )
    }
}
