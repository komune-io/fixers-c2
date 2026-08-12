package ssm.couchdb.client

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ssm.couchdb.client.test.CouchDbSandbox
import ssm.couchdb.client.test.DataTest
import ssm.couchdb.dsl.model.DocType

class CouchDbSsmServiceTest {

	@BeforeEach
	fun requireCouchDb() {
		CouchDbSandbox.assumeAvailable()
	}

	@Test
	fun shouldReturnAdmin() = runTest {
		val admin = DataTest.ssmCouchDbClient.fetchAllByDocType(DataTest.dbSsmName, DocType.Admin)
		Assertions.assertThat(admin).isNotNull
	}

	@Test
	fun shouldReturnSsmCount() = runTest {
		val ssms = DataTest.ssmCouchDbClient.fetchAllByDocType(DataTest.dbSsmName, DocType.Ssm)
		Assertions.assertThat(ssms).isNotNull
	}

	@Test
	fun shouldReturnSsm() = runTest {
		val ssmCount = DataTest.ssmCouchDbClient.getCount(DataTest.dbSsmName, DocType.Ssm)
		val ssms = DataTest.ssmCouchDbClient.fetchAllByDocType(DataTest.dbSsmName, DocType.Ssm)
		Assertions.assertThat(ssms.size).isEqualTo(ssmCount)
	}
}
