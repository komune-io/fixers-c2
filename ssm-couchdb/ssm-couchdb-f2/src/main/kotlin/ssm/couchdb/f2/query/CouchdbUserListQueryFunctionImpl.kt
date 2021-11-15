package ssm.couchdb.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.uri.burstChaincode
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbUserListQueryDTO
import ssm.couchdb.dsl.query.CouchdbUserListQueryFunction
import ssm.couchdb.dsl.query.CouchdbUserListQueryResult
import ssm.couchdb.dsl.query.CouchdbUserListQueryResultDTO
import ssm.couchdb.f2.commons.chainCodeDbName

class CouchdbUserListQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbUserListQueryFunction {

	override suspend fun invoke(msg: Flow<CouchdbUserListQueryDTO>): Flow<CouchdbUserListQueryResultDTO> =
		msg.map { payload ->
			val chaincode = payload.chaincodeUri.burstChaincode()!!
			val dbName = chainCodeDbName(chaincode.channelId, chaincode.chaincodeId)
			couchdbClient.fetchAllByDocType(dbName, DocType.User)
				.let {
					CouchdbUserListQueryResult(it)
				}
		}
}
