package ssm.couchdb.client

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ssm.couchdb.client.test.CouchDbSandbox
import ssm.couchdb.client.test.DataTest

internal class SsmCouchDbClientTest {

    @BeforeEach
    fun requireCouchDb() {
        CouchDbSandbox.assumeAvailable()
    }

    @Test
    fun `verify lastEventId is working`() {
//		val allChanges = DataTest.ssmCouchDbClient.getChanges(DataTest.ssmName)
//		Assertions.assertThat(allChanges.results).isNotEmpty
//		val last = allChanges.results.last()
//
//		val changeAfter = DataTest.ssmCouchDbClient.getChanges(DataTest.ssmName, last.seq)
//		Assertions.assertThat(changeAfter.results).isEmpty()
    }

    @Test
    suspend fun `getSsmChanges returns items carrying a sequence and an id`() {
        val allChanges = DataTest.ssmCouchDbClient.getSsmChanges(
            dbName = DataTest.dbSsmName, ssmName = DataTest.ssmName, sessionName = null
        )
        allChanges.results.forEach { item ->
            Assertions.assertThat(item.seq).isNotBlank()
            Assertions.assertThat(item.id).isNotBlank()
        }
    }
}
