package s2.spring.sourcing.ssm

import kotlin.reflect.KClass
import kotlinx.serialization.json.Json
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction

/** The chaincode and data functions an [EventPersisterSsm] writes and reads event sessions through. */
data class SsmSourcingFunctions(
    val ssmSessionStartFunction: SsmTxSessionStartFunction,
    val ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction,
    val dataSsmSessionGetQueryFunction: DataSsmSessionGetQueryFunction,
    val ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,
    val dataSsmSessionListQueryFunction: DataSsmSessionListQueryFunction,
)

/** The chaincode an [EventPersisterSsm] writes to and the agent that signs its commands. */
data class SsmSourcingTarget(
    val chaincodeUri: ChaincodeUri,
    val agentSigner: Agent,
)

/** How an [EventPersisterSsm] encodes its events into a session's public state and back. */
data class SsmEventFormat<EVENT : Any>(
    val eventType: KClass<EVENT>,
    val json: Json,
)
