package ssm.couchdb.client

import com.ibm.cloud.cloudant.v1.model.ChangesResult
import com.ibm.cloud.cloudant.v1.model.DatabaseInformation
import com.ibm.cloud.cloudant.v1.model.DesignDocument
import com.ibm.cloud.cloudant.v1.model.Document
import com.ibm.cloud.cloudant.v1.model.DocumentResult
import com.ibm.cloud.cloudant.v1.model.FindResult
import com.ibm.cloud.cloudant.v1.model.GetDatabaseInformationOptions
import com.ibm.cloud.cloudant.v1.model.GetDesignDocumentOptions
import com.ibm.cloud.cloudant.v1.model.PostChangesOptions
import com.ibm.cloud.cloudant.v1.model.PostFindOptions
import com.ibm.cloud.cloudant.v1.model.PostViewOptions
import com.ibm.cloud.cloudant.v1.model.PutDesignDocumentOptions
import com.ibm.cloud.cloudant.v1.model.ViewResult
import com.ibm.cloud.cloudant.v1.model.ViewResultRow
import com.ibm.cloud.sdk.core.http.Response
import com.ibm.cloud.sdk.core.http.ServiceCall
import com.ibm.cloud.sdk.core.service.exception.NotFoundException
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Single
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import ssm.couchdb.client.builder.CloudantFixed
import ssm.couchdb.dsl.model.DocType
import ssm.sdk.json.JSONConverter

/**
 * Offline unit tests for [CouchdbSsmClient]: the Cloudant SDK is stubbed, so no CouchDB
 * instance and no container is required. Complements [CouchDbSsmServiceTest], which only
 * runs against the sandbox.
 */
internal class CouchdbSsmClientTest {

	private val cloudant = mockk<CloudantFixed>()
	private val converter = mockk<JSONConverter>()
	private val client = CouchdbSsmClient(cloudant, converter, UnconfinedTestDispatcher())

	private companion object {
		const val DB_NAME = "ssm-db"
	}

	private fun <T> serviceCall(result: T): ServiceCall<T> {
		val response = mockk<Response<T>> { every { getResult() } returns result }
		return mockk { every { execute() } returns response }
	}

	private fun stubFind(vararg docs: Document): CapturingSlot<PostFindOptions> {
		val options = slot<PostFindOptions>()
		val findResult = mockk<FindResult> { every { getDocs() } returns docs.toList() }
		every { cloudant.postFind(capture(options)) } returns serviceCall(findResult)
		return options
	}

	private fun document(): Document = mockk(relaxed = true)

	@Test
	fun `fetchAllByDocType queries by docType and converts every document`() = runTest {
		val agents = listOf(mockk<Agent>(), mockk<Agent>())
		val options = stubFind(document(), document())
		every { converter.toObject(Agent::class.java, any()) } returnsMany agents

		val result = client.fetchAllByDocType(DB_NAME, DocType.Admin)

		Assertions.assertThat(result).isEqualTo(agents)
		Assertions.assertThat(options.captured.db()).isEqualTo(DB_NAME)
		Assertions.assertThat(options.captured.selector())
			.isEqualTo(mapOf("docType" to mapOf("\$eq" to "admin")))
	}

	@Test
	fun `fetchAllByDocType merges the extra filters into the selector`() = runTest {
		val options = stubFind()

		client.fetchAllByDocType(DB_NAME, DocType.Ssm, mapOf("name" to "ssm-1"))

		Assertions.assertThat(options.captured.selector()).isEqualTo(
			mapOf("docType" to mapOf("\$eq" to "ssm"), "name" to "ssm-1")
		)
	}

	@Test
	fun `fetchAllByDocType drops documents the converter cannot read`() = runTest {
		val agent = mockk<Agent>()
		stubFind(document(), document())
		every { converter.toObject(Agent::class.java, any()) } returnsMany listOf(agent, null)

		Assertions.assertThat(client.fetchAllByDocType(DB_NAME, DocType.Admin)).containsExactly(agent)
	}

	@Test
	fun `fetchAll returns the raw documents with an empty selector`() = runTest {
		val docs = arrayOf(document(), document())
		val options = stubFind(*docs)

		Assertions.assertThat(client.fetchAll(DB_NAME)).containsExactly(*docs)
		Assertions.assertThat(options.captured.selector()).isEmpty()
	}

	@Test
	fun `fetchOneByDocTypeAndName selects on docType and name and converts the first hit`() = runTest {
		val agent = mockk<Agent>()
		val options = stubFind(document(), document())
		every { converter.toObject(Agent::class.java, any()) } returns agent

		Assertions.assertThat(client.fetchOneByDocTypeAndName(DB_NAME, DocType.User, "bob")).isEqualTo(agent)
		Assertions.assertThat(options.captured.selector())
			.isEqualTo(mapOf("docType" to mapOf("\$eq" to "user"), "name" to "bob"))
	}

	@Test
	fun `fetchOneByDocTypeAndName returns null when nothing matches`() = runTest {
		stubFind()

		Assertions.assertThat(client.fetchOneByDocTypeAndName(DB_NAME, DocType.User, "nobody")).isNull()
	}

	@Test
	fun `getDatabases returns every database name`() = runTest {
		every { cloudant.allDbs } returns serviceCall(listOf("db-a", "db-b"))

		Assertions.assertThat(client.getDatabases()).containsExactly("db-a", "db-b")
	}

	@Test
	fun `getDatabase queries the information of the requested database`() = runTest {
		val info = mockk<DatabaseInformation>()
		val options = slot<GetDatabaseInformationOptions>()
		every { cloudant.getDatabaseInformation(capture(options)) } returns serviceCall(info)

		Assertions.assertThat(client.getDatabase(DB_NAME)).isEqualTo(info)
		Assertions.assertThat(options.captured.db()).isEqualTo(DB_NAME)
	}

	@Test
	fun `getCount reads the counting view and returns the row value`() = runTest {
		val options = stubView(rowValue = 12)

		Assertions.assertThat(client.getCount(DB_NAME, DocType.State)).isEqualTo(12)
		Assertions.assertThat(options.captured.ddoc()).isEqualTo(CouchdbSsmClient.FABRIC_COUNTING_DOC)
		Assertions.assertThat(options.captured.view()).isEqualTo(CouchdbSsmClient.COUNTING_VIEW)
		Assertions.assertThat(options.captured.key()).isEqualTo(arrayOf("state"))
	}

	@Test
	fun `getCount returns zero when the view has no row`() = runTest {
		stubView(rowValue = null)

		Assertions.assertThat(client.getCount(DB_NAME, DocType.Ssm)).isZero()
	}

	private fun stubView(rowValue: Number?): CapturingSlot<PostViewOptions> {
		val options = slot<PostViewOptions>()
		val rows = rowValue?.let { value ->
			listOf(mockk<ViewResultRow> { every { getValue() } returns value })
		} ?: emptyList()
		val viewResult = mockk<ViewResult> { every { getRows() } returns rows }
		every { cloudant.postView(capture(options)) } returns serviceCall(viewResult)
		return options
	}

	@Test
	fun `installSsmChangesFilter does nothing when the design document already exists`() = runTest {
		every { cloudant.getDesignDocument(any()) } returns serviceCall(mockk<DesignDocument>())

		Assertions.assertThat(client.installSsmChangesFilter(DB_NAME)).isFalse()
		verify(exactly = 0) { cloudant.putDesignDocument(any()) }
	}

	@Test
	fun `installSsmChangesFilter creates the filter when the design document is missing`() = runTest {
		val put = stubMissingDesignDocument()

		Assertions.assertThat(client.installSsmChangesFilter(DB_NAME)).isTrue()
		Assertions.assertThat(put.captured.db()).isEqualTo(DB_NAME)
		Assertions.assertThat(put.captured.ddoc()).isEqualTo("filters")
		Assertions.assertThat(put.captured.designDocument().filters)
			.containsEntry(CouchdbSsmClient.SSM_CHANGES_FILTER, CouchdbSsmClient.SSM_CHANGES_FILTER_FNC)
	}

	private fun stubMissingDesignDocument(): CapturingSlot<PutDesignDocumentOptions> {
		val getOptions = slot<GetDesignDocumentOptions>()
		val failing = mockk<ServiceCall<DesignDocument>> {
			every { execute() } throws NotFoundException(mockk(relaxed = true))
		}
		every { cloudant.getDesignDocument(capture(getOptions)) } returns failing

		val putOptions = slot<PutDesignDocumentOptions>()
		val putCall = mockk<ServiceCall<DocumentResult>> {
			every { reactiveRequest() } returns Single.just(mockk<Response<DocumentResult>>())
		}
		every { cloudant.putDesignDocument(capture(putOptions)) } returns putCall
		return putOptions
	}

	@Test
	fun `getSsmChanges installs the filter and forwards ssm, session and paging`() = runTest {
		stubMissingDesignDocument()
		val changes = mockk<ChangesResult>()
		val options = slot<PostChangesOptions>()
		every {
			cloudant.postChanges(capture(options), "ssm-1", "session-1")
		} returns serviceCall(changes)

		val result = client.getSsmChanges(DB_NAME, "ssm-1", "session-1", lastEventId = "42", limit = 10)

		Assertions.assertThat(result).isEqualTo(changes)
		Assertions.assertThat(options.captured.db()).isEqualTo(DB_NAME)
		Assertions.assertThat(options.captured.lastEventId()).isEqualTo("42")
		Assertions.assertThat(options.captured.limit()).isEqualTo(10)
		Assertions.assertThat(options.captured.includeDocs()).isTrue()
		Assertions.assertThat(options.captured.filter())
			.isEqualTo("filters/${CouchdbSsmClient.SSM_CHANGES_FILTER}")
		verify { cloudant.putDesignDocument(any()) }
	}

	@Test
	fun `getSsmChanges leaves the limit unset when none is given`() = runTest {
		every { cloudant.getDesignDocument(any()) } returns serviceCall(mockk<DesignDocument>())
		val options = slot<PostChangesOptions>()
		every { cloudant.postChanges(capture(options), any(), any()) } returns serviceCall(mockk<ChangesResult>())

		client.getSsmChanges(DB_NAME, "ssm-1", sessionName = null)

		Assertions.assertThat(options.captured.limit() as Long?).isNull()
	}

	/**
	 * The reason this client is `suspend` in the first place: the Cloudant SDK is blocking, so
	 * the call must leave the caller's thread and run on the injected dispatcher.
	 */
	@Test
	fun `calls run on the injected dispatcher and not on the caller thread`() {
		val executor = Executors.newSingleThreadExecutor { runnable ->
			Thread(runnable, "couchdb-dispatcher-test")
		}
		try {
			val dispatched = CouchdbSsmClient(cloudant, converter, executor.asCoroutineDispatcher())
			lateinit var callThread: String
			every { cloudant.allDbs } answers {
				callThread = Thread.currentThread().name
				serviceCall(emptyList())
			}

			runTest { dispatched.getDatabases() }

			Assertions.assertThat(callThread).isEqualTo("couchdb-dispatcher-test")
			Assertions.assertThat(callThread).isNotEqualTo(Thread.currentThread().name)
		} finally {
			executor.shutdownNow()
		}
	}
}
