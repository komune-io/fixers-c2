package ssm.couchdb.client.test

import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.client.builder.SsmCouchDbBasicAuth

object DataTest {
	const val dbSsmName = "sandbox_ssm"
	const val ssmName = "ssm"

	private const val username = "couchdb"
	private const val password = "couchdb"
	private const val serviceUrl = "http://localhost:5984"

	var ssmCouchDbClient: CouchdbSsmClient = CouchdbSsmClient.builder()
		.withUrl(serviceUrl)
		.withName("Ssm Sdk Unit Test")
		.withAuth(
			SsmCouchDbBasicAuth(
			username = username,
			password = password,
		)
		).build()
}
