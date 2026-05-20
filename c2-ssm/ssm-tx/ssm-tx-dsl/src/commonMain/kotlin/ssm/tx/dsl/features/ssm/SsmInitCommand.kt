package ssm.tx.dsl.features.ssm

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.tx.dsl.features.SsmCommandDTO

/**
 * Initializes an SSM
 * @d2 function
 * @parent [ssm.tx.dsl.SsmTxD2Command]
 * @title Initialize SSM
 */
typealias SsmTxInitFunction = F2Function<SsmInitCommand, SsmInitdResult>

/**
 * @d2 command
 * @parent [SsmTxInitFunction]
 * @title Initialize SSM: Parameters
 */
class SsmInitCommand(
    override val chaincodeUri: ChaincodeUriDTO,
    /**
	 * The name of the signer
	 */
	val signerName: AgentName,
    /**
	 * Description of the SSM to create
	 */
	val ssm: Ssm,

    /**
	 * Initial user of the SSM
	 */
	val agent: Agent,
) : SsmCommandDTO

/**
 * @d2 event
 * @parent [SsmTxInitFunction]
 * @title Initialize SSM: Response
 */
class SsmInitdResult(
	val results: List<TransactionId>
) : Event
