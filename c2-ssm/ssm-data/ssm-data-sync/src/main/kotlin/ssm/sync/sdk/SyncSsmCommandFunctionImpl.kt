package ssm.sync.sdk

import f2.dsl.fnc.invokeWith
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.couchdb.dsl.model.DatabaseChangesDTO
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQuery
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryResultDTO
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQuery
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryFunction
import ssm.data.dsl.features.query.DataSsmGetQuery
import ssm.data.dsl.features.query.DataSsmGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogListQuery
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryFunction

class SyncSsmCommandFunctionImpl(
	private val dataSsmGetQueryFunction: DataSsmGetQueryFunction,
	private val dataSsmSessionLogListQueryFunction: DataSsmSessionLogListQueryFunction,
	private val couchdbSsmSessionStateGetQueryFunction: CouchdbSsmSessionStateGetQueryFunction,
	private val couchdbDatabaseGetChangesQueryFunction: CouchdbDatabaseGetChangesQueryFunction
) : SyncSsmCommandFunction {

	override suspend fun invoke(msgs: Flow<SyncSsmCommand>): Flow<SyncSsmCommandResult> =
		msgs.map { payload ->
			val result = payload.queryCouchdbDatabaseGetChanges()
			result.items.applySsmSessionChanges(payload.chaincodeUri).fold()
				.toResult()
				.let {
					SyncSsmCommandResult(
						lastEventId = result.lastEventId,
						items = it
					)
				}
		}

	private suspend fun List<SsmSyncChanges>?.toResult() = this?.map { changes ->
		changes.sessions.map { name ->
			toResult(changes.ssm, name)
		}
	}?.flatten() ?: emptyList()

	private suspend fun toResult(uri: SsmUri, name: SessionName): SsmSessionSyncResult {
		return DataSsmSessionLogListQuery(
			sessionName = name,
			ssmUri = uri
		).invokeWith(dataSsmSessionLogListQueryFunction).let { result ->
			SsmSessionSyncResult(
				ssm = uri,
				name = name,
				logs = result.items
			)
		}
	}

	private suspend fun SyncSsmCommand.queryCouchdbDatabaseGetChanges()
	: CouchdbDatabaseGetChangesQueryResultDTO = CouchdbDatabaseGetChangesQuery(
		chaincodeId = chaincodeUri.chaincodeId,
		channelId = chaincodeUri.channelId,
		lastEventId = lastEventId,
		ssmName = ssmName,
		sessionName = sessionName,
		limit = limit
	).invokeWith(
			couchdbDatabaseGetChangesQueryFunction
	)

	private suspend fun List<DatabaseChangesDTO>.applySsmSessionChanges(
		uri: ChaincodeUri
	): List<SsmSyncChanges> = mapNotNull { change ->
		when (change.docType) {
			is DocType.Ssm -> fetchSsm(uri, change)
			is DocType.State -> fetchSession(uri, change)
			else -> null
		}
	}

	private suspend fun fetchSession(uri: ChaincodeUri, change: DatabaseChangesDTO): SsmSyncChanges {
		return CouchdbSsmSessionStateGetQuery(
			chaincodeUri = uri,
			sessionName = change.objectId,
			ssmName = null
		)
			.invokeWith(couchdbSsmSessionStateGetQueryFunction)
			.let { result ->
				SsmSyncChanges(
					ssm = uri.toSsmUri(result.item.ssm!!),
					sessions = setOf(result.item.session)
				)
			}
	}

	private suspend fun fetchSsm(uri: ChaincodeUri, change: DatabaseChangesDTO): SsmSyncChanges {
		return DataSsmGetQuery(
			ssmUri = uri.toSsmUri(change.objectId)
		).invokeWith(dataSsmGetQueryFunction).item!!.let { ssm ->
			SsmSyncChanges(
				ssm.uri
			)
		}
	}

	private fun List<SsmSyncChanges>.fold() = groupBy({ it.ssm }, { it.sessions })
		.toList()
		.map { (uri, sessionNames) ->
			SsmSyncChanges(
				ssm = uri,
				sessions = sessionNames.flatten().toSet()
			)
		}

	data class SsmSyncChanges(
		val ssm: SsmUri,
		val sessions: Set<SessionName> = emptySet()
	)
}
