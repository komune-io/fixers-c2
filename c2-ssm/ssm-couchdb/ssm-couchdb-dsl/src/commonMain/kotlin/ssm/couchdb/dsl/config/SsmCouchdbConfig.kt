package ssm.couchdb.dsl.config

/**
 * Properties need to connect to couchdb.
 * @d2 model
 * @title CouchdbConfig
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Configuration]
 */
data class SsmCouchdbConfig(
    /**
     * URL of the machine hosting the database.
     * @example "http://peer.sandbox.Komune.io:9000"
     */
    val url: String,

    /**
     * Username to log into the service handling the database.
     * @example "admin"
     */
    val username: String,

    /**
     * Password to log into the service handling the database.
     * @example "admin"
     */
    val password: String,

    /**
     * Name of the service handling the database.
     * @example "ssm-couchdb"
     */
    val serviceName: String,
)

typealias CouchdbUrl = String
typealias CouchdbUsername = String
typealias CouchdbPassword = String
typealias CouchdbServiceName = String
