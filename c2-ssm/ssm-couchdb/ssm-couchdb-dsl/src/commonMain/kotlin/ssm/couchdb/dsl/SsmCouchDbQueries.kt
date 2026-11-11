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
 * The query functions of the CouchDB SSM store.
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
    // @JsExport member: the name is the JS API and the F2 bean the starter publishes.
    @Suppress("FunctionNameMaxLength")
    fun couchdbDatabaseGetChangesQueryFunction(): CouchdbDatabaseGetChangesQueryFunction
    // @JsExport member: the name is the JS API and the F2 bean the starter publishes.
    @Suppress("FunctionNameMaxLength")
    fun couchdbDatabaseListQueryFunction(): CouchdbDatabaseListQueryFunction
    // @JsExport member: the name is the JS API and the F2 bean the starter publishes.
    @Suppress("FunctionNameMaxLength")
    fun couchdbDatabaseGetQueryFunction(): CouchdbDatabaseGetQueryFunction

    // @JsExport member: the name is the JS API and the F2 bean the starter publishes.
    @Suppress("FunctionNameMaxLength")
    fun couchdbChaincodeListQueryFunction(): CouchdbChaincodeListQueryFunction

    fun couchdbAdminListQueryFunction(): CouchdbAdminListQueryFunction

    fun couchdbUserListQueryFunction(): CouchdbUserListQueryFunction

    fun couchdbSsmGetQueryFunction(): CouchdbSsmGetQueryFunction
    fun couchdbSsmListQueryFunction(): CouchdbSsmListQueryFunction

    // @JsExport member: the name is the JS API and the F2 bean the starter publishes.
    @Suppress("FunctionNameMaxLength")
    fun couchdbSsmSessionStateListQueryFunction(): CouchdbSsmSessionStateListQueryFunction
    // @JsExport member: the name is the JS API and the F2 bean the starter publishes.
    @Suppress("FunctionNameMaxLength")
    fun couchdbSsmSessionStateGetQueryFunction(): CouchdbSsmSessionStateGetQueryFunction
}
