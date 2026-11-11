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
import s2.spring.automate.ssm.persister.SsmSessionFunctions
import s2.spring.automate.ssm.persister.SsmSigningTarget
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.tx.dsl.features.ssm.SsmInitCommand
import ssm.tx.dsl.features.ssm.SsmTxInitFunction
import tools.jackson.databind.ObjectMapper

abstract class S2SsmConfigurerAdapter<STATE, ID : Any, ENTITY, AGGREGATE> :
    S2ConfigurerAdapter<STATE, ID, ENTITY, AGGREGATE>() where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>,
AGGREGATE : S2AutomateExecutorSpring<STATE, ID, ENTITY> {

    // Field injection: consumers subclass this adapter and must not have to forward these beans.
    @Suppress("LateinitUsage")
    @Autowired
    lateinit var ssmTxInitFunction: SsmTxInitFunction

    // Field injection: consumers subclass this adapter and must not have to forward these beans.
    @Suppress("LateinitUsage")
    @Autowired
    lateinit var ssmSessionStartFunction: SsmTxSessionStartFunction

    // Field injection: consumers subclass this adapter and must not have to forward these beans.
    @Suppress("LateinitUsage")
    @Autowired
    lateinit var ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction

    // Field injection: consumers subclass this adapter and must not have to forward these beans.
    @Suppress("LateinitUsage")
    @Autowired
    lateinit var ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction

    // Field injection: consumers subclass this adapter and must not have to forward these beans.
    @Suppress("LateinitUsage")
    @Autowired
    lateinit var objectMapper: ObjectMapper

    open var isPermissive = false

    override fun aggregateRepository(): AutomatePersister<STATE, ID, ENTITY, Evt, S2Automate> = runBlocking {
        val automate = automate()
        val signer = signerAgent()
        val chaincodeUri = chaincodeUri()
        SsmAutomatePersister<STATE, ID, ENTITY, Evt>(
            functions = SsmSessionFunctions(
                ssmSessionStartFunction = ssmSessionStartFunction,
                ssmSessionPerformActionFunction = ssmSessionPerformActionFunction,
                ssmGetSessionLogsQueryFunction = ssmGetSessionLogsQueryFunction,
            ),
            target = SsmSigningTarget(chaincodeUri = chaincodeUri, agentSigner = signer),
            entityType = entityType(),
            objectMapper = objectMapper,
            batch = batchParams,
            timestampProvider = ::businessTimestampOf
        ).also {
            val _ = ssmTxInitFunction.invoke(
                SsmInitCommand(
                    signerName = signer.name,
                    ssm = automate.toSsm(permissive = isPermissive),
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
}
