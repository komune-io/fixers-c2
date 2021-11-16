package ssm.sync.sdk

import f2.dsl.fnc.F2Function
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.couchdb.dsl.model.ChangeEventId
import ssm.data.dsl.model.DataSsmSessionState

typealias SyncSsmCommandFunction = F2Function<SyncSsmCommand, SyncSsmCommandResult>

interface SyncSsmCommandDTO {
	val lastEventId: ChangeEventId?
	val chaincodeUri: ChaincodeUri
	val ssmName: SsmName?
	val sessionName: SessionName?
}

interface SyncSsmCommandResultDTO {
	val lastEventId: ChangeEventId?
	val items: List<SsmSessionSyncResult>
}

class SyncSsmCommand(
	override val lastEventId: ChangeEventId?,
	override val chaincodeUri: ChaincodeUri,
	override val ssmName: SsmName? = null,
	override val sessionName: SessionName? = null,
) : SyncSsmCommandDTO

data class SyncSsmCommandResult(
	override val lastEventId: ChangeEventId?,
	override val items: List<SsmSessionSyncResult>
): SyncSsmCommandResultDTO

data class SsmSessionSyncResult(
	val ssm: SsmUri,
	val name: SessionName,
	val logs: List<DataSsmSessionState>
)
