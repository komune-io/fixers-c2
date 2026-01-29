package ssm.sdk.core

import ssm.chaincode.dsl.blockchain.Block
import ssm.chaincode.dsl.blockchain.BlockId
import ssm.chaincode.dsl.blockchain.Transaction
import ssm.chaincode.dsl.blockchain.TransactionId
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.uri.ChaincodeUri

@Suppress("TooManyFunctions")
interface SsmQueryServiceI {
	suspend fun listAdmins(chaincodeUri: ChaincodeUri): List<AgentName>
	suspend fun getAdmin(chaincodeUri: ChaincodeUri, username: AgentName): Agent?
	suspend fun listUsers(chaincodeUri: ChaincodeUri): List<AgentName>
	suspend fun getAgent(chaincodeUri: ChaincodeUri, agentName: AgentName): Agent?
	suspend fun listSsm(chaincodeUri: ChaincodeUri): List<SsmName>
	suspend fun getSsm(chaincodeUri: ChaincodeUri, name: SsmName): Ssm?
	suspend fun getSession(chaincodeUri: ChaincodeUri,sessionName: SessionName): SsmSessionState?
	suspend fun log(chaincodeUri: ChaincodeUri, sessionName: SessionName): List<SsmSessionStateLog>
	suspend fun listSession(chaincodeUri: ChaincodeUri): List<String>
	suspend fun getTransaction(chaincodeUri: ChaincodeUri, txId: TransactionId): Transaction?
	suspend fun getBlock(chaincodeUri: ChaincodeUri, blockId: BlockId): Block?

	suspend fun getAdmins(queries: List<GetAdminQuery>): List<Agent>
	suspend fun getAgents(queries: List<GetAgentQuery>): List<Agent>
	suspend fun getSsms(queries: List<GetSsmQuery>): List<Ssm>
	suspend fun getSessions(queries: List<GetSessionQuery>): List<SsmSessionState?>
	suspend fun getTransactions(queries: List<GetTransactionQuery>): List<Transaction?>
	suspend fun getBlocks(queries: List<GetBlockQuery>): List<Block>
	suspend fun getLogs(queries: List<GetLogQuery>): List<List<SsmSessionStateLog>>
}

data class GetAdminQuery(val chaincodeUri: ChaincodeUri, val username: AgentName)
data class GetAgentQuery(val chaincodeUri: ChaincodeUri, val agentName: AgentName)
data class GetSsmQuery(val chaincodeUri: ChaincodeUri, val name: SsmName)
data class GetSessionQuery(val chaincodeUri: ChaincodeUri, val sessionName: SessionName)
data class GetTransactionQuery(val chaincodeUri: ChaincodeUri, val txId: TransactionId)
data class GetBlockQuery(val chaincodeUri: ChaincodeUri, val blockId: BlockId)
data class GetLogQuery(val chaincodeUri: ChaincodeUri, val sessionName: SessionName)
