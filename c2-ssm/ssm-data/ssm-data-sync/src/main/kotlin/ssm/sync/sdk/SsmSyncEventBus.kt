package ssm.sync.sdk

import f2.dsl.fnc.invokeWith
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.couchdb.dsl.model.ChangeEventId

class SsmSyncEventBus (
    private val chaincodeUri: ChaincodeUri,
    private val ssmName: SsmName,
    private val sessionName: SessionName? = null,
    private val syncSsmCommandFunction: SyncSsmCommandFunction
) {

	fun sync(lastEventId: ChangeEventId? = null, delay: Long = 5000, limit: Long = 20): Flow<SyncSsmCommandResult> = flow {
			var changeEventId = lastEventId
			while (true) {
				SyncSsmCommand(
					lastEventId = changeEventId,
					chaincodeUri = chaincodeUri,
					ssmName = ssmName,
					sessionName = sessionName,
					limit = limit
				).invokeWith(syncSsmCommandFunction).let { result ->
					changeEventId = result.lastEventId
					emit(result)
				}
				delay(delay)
			}
	}
}
