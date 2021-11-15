package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.uri.SsmUriBurst
import ssm.chaincode.dsl.model.uri.compact
import ssm.couchdb.client.SsmCouchdbClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbSsmGetQuery
import ssm.couchdb.dsl.query.CouchdbSsmGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmGetQueryResult
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbSsmGetQueryFunctionImpl(
	private val couchdbClient: SsmCouchdbClient,
) : CouchdbSsmGetQueryFunction {

	override suspend fun invoke(msg: Flow<CouchdbSsmGetQuery>): Flow<CouchdbSsmGetQueryResult> = msg.map { payload ->
		couchdbClient
			.fetchOneByDocTypeAndName(chainCodeDbName(payload.channelId, payload.chaincodeId), DocType.Ssm, payload.ssmName)
			.let{ item ->
				CouchdbSsmGetQueryResult(
					item = item,
					uri = SsmUriBurst(
						channelId = payload.channelId,
						chaincodeId = payload.chaincodeId,
						ssmName = payload.ssmName,
					).compact()
				)
			}
	}
}
