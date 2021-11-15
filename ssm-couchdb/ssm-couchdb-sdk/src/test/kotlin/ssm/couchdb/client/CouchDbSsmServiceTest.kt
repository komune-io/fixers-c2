package ssm.couchdb.client

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.couchdb.client.test.DataTest
import ssm.couchdb.dsl.model.DocType

class CouchDbSsmServiceTest {

	@Test
	fun shouldReturnAdmin() {
		val admin = DataTest.ssmCouchDbClient.fetchAllByDocType(DataTest.ssmName, DocType.Admin)
		Assertions.assertThat(admin).isNotNull
	}

	@Test
	fun shouldReturnSsmCount() {
		val ssms = DataTest.ssmCouchDbClient.fetchAllByDocType(DataTest.ssmName, DocType.Ssm)
		Assertions.assertThat(ssms).isNotNull
	}

	@Test
	fun shouldReturnSsm() {
		val ssmCount = DataTest.ssmCouchDbClient.getCount(DataTest.ssmName, DocType.Ssm)
		val ssms = DataTest.ssmCouchDbClient.fetchAllByDocType(DataTest.ssmName, DocType.Ssm)
		Assertions.assertThat(ssms.size).isEqualTo(ssmCount)
	}
}
