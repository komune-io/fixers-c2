package ssm.tx.dsl.features.ssm

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.tx.dsl.features.SsmCommandDTO
import ssm.tx.dsl.features.SsmCommandResultDTO

/**
 * Creates an SSM
 * @d2 function
 * @parent [ssm.tx.dsl.SsmTxD2Command]
 * @title Create SSM
 */
typealias SsmTxCreateFunction = F2Function<SsmCreateCommand, SsmCreateResult>

/**
 * @d2 command
 * @parent [SsmTxCreateFunction]
 * @title Create SSM: Parameters
 */
class SsmCreateCommand(
    override val chaincodeUri: ChaincodeUriDTO,
    val signerName: AgentName,
    /**
	 * Description of the SSM to create
	 */
	val ssm: Ssm,
) : SsmCommandDTO

/**
 * @d2 event
 * @parent [SsmTxCreateFunction]
 * @title Create SSM: Response
 */
class SsmCreateResult(
	override val transactionId: TransactionId
) : SsmCommandResultDTO
