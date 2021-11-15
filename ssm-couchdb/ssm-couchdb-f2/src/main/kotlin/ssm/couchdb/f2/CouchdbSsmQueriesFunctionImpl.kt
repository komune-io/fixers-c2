package ssm.couchdb.f2

import ssm.couchdb.client.SsmCouchdbClient
import ssm.couchdb.client.builder.SsmCouchDbBasicAuth
import ssm.couchdb.dsl.SsmCouchDbQueries
import ssm.couchdb.dsl.config.SsmCouchdbConfig
import ssm.couchdb.dsl.query.CouchdbAdminListQueryFunction
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseListQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmListQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryFunction
import ssm.couchdb.dsl.query.CouchdbUserListQueryFunction
import ssm.couchdb.f2.query.CouchDbDatabaseGetChangesQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbAdminListQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbChaincodeListQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbDatabaseGetQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbDatabaseListQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbSsmGetQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbSsmListQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbSsmSessionStateGetQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbSsmSessionStateListQueryFunctionImpl
import ssm.couchdb.f2.query.CouchdbUserListQueryFunctionImpl

class CouchdbSsmQueriesFunctionImpl(
	config: SsmCouchdbConfig
) : SsmCouchDbQueries {

	private val couchdbClient = SsmCouchdbClient.builder()
		.withUrl(config.url)
		.withName(config.serviceName)
		.withAuth(
			SsmCouchDbBasicAuth(
				username = config.username,
				password = config.password,
			)
		).build()

	override fun couchdbDatabaseGetChangesQueryFunction(): CouchdbDatabaseGetChangesQueryFunction {
		return CouchDbDatabaseGetChangesQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbDatabaseListQueryFunction(): CouchdbDatabaseListQueryFunction {
		return CouchdbDatabaseListQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbDatabaseGetQueryFunction(): CouchdbDatabaseGetQueryFunction {
		return CouchdbDatabaseGetQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbChaincodeListQueryFunction(): CouchdbChaincodeListQueryFunction {
		return CouchdbChaincodeListQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbAdminListQueryFunction(): CouchdbAdminListQueryFunction {
		return CouchdbAdminListQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbUserListQueryFunction(): CouchdbUserListQueryFunction {
		return CouchdbUserListQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbSsmGetQueryFunction(): CouchdbSsmGetQueryFunction {
		return CouchdbSsmGetQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbSsmListQueryFunction(): CouchdbSsmListQueryFunction {
		return CouchdbSsmListQueryFunctionImpl(couchdbClient)
	}

	override fun couchdbSsmSessionStateListQueryFunction(): CouchdbSsmSessionStateListQueryFunction {
		return CouchdbSsmSessionStateListQueryFunctionImpl(couchdbClient)
	}


	override fun couchdbSsmSessionStateGetQueryFunction(): CouchdbSsmSessionStateGetQueryFunction {
		return CouchdbSsmSessionStateGetQueryFunctionImpl(couchdbClient)
	}
}
