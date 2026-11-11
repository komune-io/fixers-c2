package ssm.couchdb.dsl

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
// Kotlin only: F2Function is not exported to JS, which reaches these functions through f2's JS facade.
interface SsmCouchDbQueries {
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun couchdbDatabaseGetChangesQueryFunction(): CouchdbDatabaseGetChangesQueryFunction
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun couchdbDatabaseListQueryFunction(): CouchdbDatabaseListQueryFunction
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun couchdbDatabaseGetQueryFunction(): CouchdbDatabaseGetQueryFunction

    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun couchdbChaincodeListQueryFunction(): CouchdbChaincodeListQueryFunction

    fun couchdbAdminListQueryFunction(): CouchdbAdminListQueryFunction

    fun couchdbUserListQueryFunction(): CouchdbUserListQueryFunction

    fun couchdbSsmGetQueryFunction(): CouchdbSsmGetQueryFunction
    fun couchdbSsmListQueryFunction(): CouchdbSsmListQueryFunction

    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun couchdbSsmSessionStateListQueryFunction(): CouchdbSsmSessionStateListQueryFunction
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun couchdbSsmSessionStateGetQueryFunction(): CouchdbSsmSessionStateGetQueryFunction
}
