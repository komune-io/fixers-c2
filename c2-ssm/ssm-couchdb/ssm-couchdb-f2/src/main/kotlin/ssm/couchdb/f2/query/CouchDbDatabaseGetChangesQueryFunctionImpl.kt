package ssm.couchdb.f2.query

import com.ibm.cloud.cloudant.v1.model.ChangesResult
import com.ibm.cloud.cloudant.v1.model.ChangesResultItem
import kotlinx.coroutines.CancellationException
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

    // F2Function declares invoke suspend: an override cannot drop it; any failure answers an empty result.
    @Suppress("SuspendFunWithFlowReturnType", "kotlin:S6309", "TooGenericExceptionCaught")
    override suspend fun invoke(msgs: Flow<CouchdbDatabaseGetChangesQueryDTO>)
    : Flow<CouchdbDatabaseGetChangesQueryResultDTO> = msgs.map { payload ->
        try {
            getChanges(couchdbClient, payload)
        } catch (e: CancellationException) {
            throw e
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
        logger.info(
            "GetChanges for {}:{}:{}:{}",
            payload.channelId,
            payload.chaincodeId,
            payload.ssmName,
            payload.sessionName.orEmpty(),
        )
        val dbName = chainCodeDbName(channelId = payload.channelId, chaincodeId = payload.chaincodeId)
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
        }.let { items ->
            CouchdbDatabaseGetChangesQueryResult(
                items = items,
                lastEventId = lastSeq
            )
        }
    }

    private fun ChangesResultItem.parseId(): String = id.substringAfter("_")
}
