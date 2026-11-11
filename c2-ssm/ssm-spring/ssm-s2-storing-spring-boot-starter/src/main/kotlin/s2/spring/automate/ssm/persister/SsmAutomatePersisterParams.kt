package s2.spring.automate.ssm.persister

import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction

/** The chaincode functions an [SsmAutomatePersister] starts, advances and reads sessions through. */
data class SsmSessionFunctions(
    val ssmSessionStartFunction: SsmTxSessionStartFunction,
    val ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction,
    val ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
)

/** The chaincode an [SsmAutomatePersister] writes to and the agent that signs its commands. */
data class SsmSigningTarget(
    val chaincodeUri: ChaincodeUri,
    val agentSigner: Agent,
)
