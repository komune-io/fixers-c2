package ssm.couchdb.f2.query

import io.komune.c2.chaincode.dsl.burst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.SsmSessionStateDTO
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryDTO
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryResult
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbSsmSessionStateGetQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbSsmSessionStateGetQueryFunction {

	override suspend fun invoke(msgs: Flow<CouchdbSsmSessionStateGetQueryDTO>):
			Flow<CouchdbSsmSessionStateGetQueryResultDTO> = msgs.map { payload ->
		val filters = mapOf(SsmSessionStateDTO::session.name to payload.sessionName)
		val uri = payload.chaincodeUri.burst()
		couchdbClient.fetchAllByDocType(chainCodeDbName(uri.channelId, uri.chaincodeId), DocType.State, filters)
			.let { list ->
				CouchdbSsmSessionStateGetQueryResult(
					item = list.first()
				)
			}
	}
}
