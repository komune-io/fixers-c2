package ssm.tx.dsl.features.user

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.tx.dsl.features.SsmCommandDTO
import ssm.tx.dsl.features.SsmCommandResultDTO

/**
 * Grant user
 * @d2 function
 * @parent [ssm.tx.dsl.SsmTxD2Command]
 * @title Grant User
 */
typealias SsmTxUserGrantFunction = F2Function<SsmUserGrantCommand, SsmUserGrantedResult>

/**
 * @d2 command
 * @parent [SsmTxUserGrantFunction]
 * @title Grant User: Parameters
 */
class SsmUserGrantCommand(
    override val chaincodeUri: ChaincodeUriDTO,
    /**
	 * The name of the signer
	 */
	val signerName: AgentName,
    /**
	 * Initial user of the SSM
	 */
	val agent: Agent,
) : SsmCommandDTO

/**
 * @d2 event
 * @parent [SsmTxUserGrantFunction]
 * @title Grant User: Response
 */
class SsmUserGrantedResult(
    override val transactionId: TransactionId,
) : SsmCommandResultDTO
