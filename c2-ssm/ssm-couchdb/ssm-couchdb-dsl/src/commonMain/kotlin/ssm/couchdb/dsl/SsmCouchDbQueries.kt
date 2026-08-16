package ssm.couchdb.dsl

import kotlin.js.JsExport
import kotlin.js.JsName
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

/**
 * - fun couchDbDatabaseGetChangesQueryFunction(): [ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryFunction]
 * - fun couchdbDatabaseListQueryFunction(): [ssm.couchdb.dsl.query.CouchdbDatabaseListQueryFunction]
 * - fun couchDbDatabaseGetQueryFunction(): [ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryFunction]
 * - fun couchdbSsmGetQueryFunction(): [ssm.couchdb.dsl.query.CouchdbSsmGetQueryFunction]
 * - fun couchDbSsmListQueryFunction(): [ssm.couchdb.dsl.query.CouchdbSsmListQueryFunction]
 * - fun couchDbSsmSessionStateListQueryFunction(): [ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryFunction]
 * @d2 model
 * @title Query function
 * @parent [CouchdbSsmD2]
 */
@JsExport
@JsName("SsmCouchDbQueries")
interface SsmCouchDbQueries {
	fun couchdbDatabaseGetChangesQueryFunction(): CouchdbDatabaseGetChangesQueryFunction
	fun couchdbDatabaseListQueryFunction(): CouchdbDatabaseListQueryFunction
	fun couchdbDatabaseGetQueryFunction(): CouchdbDatabaseGetQueryFunction

	fun couchdbChaincodeListQueryFunction(): CouchdbChaincodeListQueryFunction

	fun couchdbAdminListQueryFunction(): CouchdbAdminListQueryFunction

	fun couchdbUserListQueryFunction(): CouchdbUserListQueryFunction

	fun couchdbSsmGetQueryFunction(): CouchdbSsmGetQueryFunction
	fun couchdbSsmListQueryFunction(): CouchdbSsmListQueryFunction

	fun couchdbSsmSessionStateListQueryFunction(): CouchdbSsmSessionStateListQueryFunction
	fun couchdbSsmSessionStateGetQueryFunction(): CouchdbSsmSessionStateGetQueryFunction
}
