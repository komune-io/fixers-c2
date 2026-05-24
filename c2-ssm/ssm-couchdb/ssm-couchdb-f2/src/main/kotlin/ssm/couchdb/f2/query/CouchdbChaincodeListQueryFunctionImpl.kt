package ssm.couchdb.f2.query

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryDTO
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryFunction
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryResult
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryResultDTO

class CouchdbChaincodeListQueryFunctionImpl(
	private val couchdbClient: CouchdbSsmClient,
) : CouchdbChaincodeListQueryFunction {
	companion object {
		const val DB_LSCC = "_lscc"
	}

	override suspend fun invoke(
		msgs: Flow<CouchdbChaincodeListQueryDTO>
	): Flow<CouchdbChaincodeListQueryResultDTO> = msgs.map { _ ->
		couchdbClient.cloudant.allDbs.execute()
			.result
			.asFlow()
			.filter { it.contains(DB_LSCC) }
			.flatMapMerge { dbName ->
				val channelId = dbName.removeSuffix(DB_LSCC)
				couchdbClient.fetchAll(dbName).map { document ->
					document.id
				}.map { chaincodeId ->
					ChaincodeUri.from(
						channelId = channelId,
						chaincodeId = chaincodeId
					)
				}.asFlow()
			}.toList()
			.let {
				CouchdbChaincodeListQueryResult(
					items = it
				)
			}

	}

}
