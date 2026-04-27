package ssm.couchdb.f2.query

import com.ibm.cloud.cloudant.v1.model.ChangesResult
import com.ibm.cloud.cloudant.v1.model.ChangesResultItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.client.getDocType
import ssm.couchdb.dsl.model.DatabaseChanges
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryDTO
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryResult
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchDbDatabaseGetChangesQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbDatabaseGetChangesQueryFunction {

	private val logger = LoggerFactory.getLogger(CouchDbDatabaseGetChangesQueryFunctionImpl::class.java)

	@Suppress("TooGenericExceptionCaught")
	override suspend fun invoke(msgs: Flow<CouchdbDatabaseGetChangesQueryDTO>)
	: Flow<CouchdbDatabaseGetChangesQueryResultDTO> = msgs.map { payload ->
		try {
			getChanges(couchdbClient, payload)
		} catch (e: Exception) {
			logger.error(e.message, e)
			CouchdbDatabaseGetChangesQueryResult(
				items = emptyList(),
				lastEventId = null
			)
		}
	}

	private suspend fun getChanges(
		couchdbClient: CouchdbSsmClient,
		payload: CouchdbDatabaseGetChangesQueryDTO
	): CouchdbDatabaseGetChangesQueryResultDTO {
		logger.info("GetChanges for ${payload.channelId}:${payload.chaincodeId}:${payload.ssmName}:${payload.sessionName}")
		val dbName = chainCodeDbName(payload.channelId, payload.chaincodeId)
		return couchdbClient.getSsmChanges(
			dbName = dbName,
			lastEventId = payload.lastEventId,
			limit = payload.limit,
			ssmName = payload.ssmName,
			sessionName = payload.sessionName
		).transformResult()
	}

	private fun ChangesResult.transformResult(): CouchdbDatabaseGetChangesQueryResult {
		return results.filter { changes ->
			changes.parseId().isNotBlank()
		}.map { changesItem ->
			DatabaseChanges(
				objectId = changesItem.parseId(),
				docType = changesItem.getDocType(),
				changeEventId = changesItem.seq
			)
		}.let {
			CouchdbDatabaseGetChangesQueryResult(
				items = it,
				lastEventId = lastSeq
			)
		}
	}

	private fun ChangesResultItem.parseId(): String {
		return id.substringAfter("_")
	}
}
