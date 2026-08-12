package ssm.couchdb.f2.query

import f2.dsl.cqrs.page.OffsetPagination
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.dsl.model.DocType
import ssm.couchdb.dsl.query.CouchdbSsmListQuery
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQuery

/**
 * Verifies the list queries push the requested page down to CouchDB and report a total that
 * reflects the whole selection rather than the page. Runs against a mocked client: no CouchDB.
 */
internal class ListQueryPaginationTest {

	private companion object {
		const val CHANNEL_ID = "sandbox"
		const val CHAINCODE_ID = "ssm"
		const val DB_NAME = "sandbox_ssm"
	}

	private val client = mockk<CouchdbSsmClient>()

	private fun ssm(name: String) = Ssm(name = name, transitions = emptyList())

	private fun ssmListQuery(pagination: OffsetPagination?) = CouchdbSsmListQuery(
		pagination = pagination,
		channelId = CHANNEL_ID,
		chaincodeId = CHAINCODE_ID,
	)

	@Test
	suspend fun `ssm list pushes limit and skip down and reports the unpaged total`() {
		val page = listOf(ssm("ssm-3"), ssm("ssm-4"))
		every { client.fetchAllByDocType(DB_NAME, DocType.Ssm, any(), 2L, 2L) } returns page
		every { client.countByDocType(DB_NAME, DocType.Ssm, any()) } returns 7

		val result = CouchdbSsmListQueryFunctionImpl(client)
			.invoke(flowOf(ssmListQuery(OffsetPagination(offset = 2, limit = 2))))
			.toList()
			.single()

		assertThat(result.items).isEqualTo(page)
		assertThat(result.total).isEqualTo(7)
		assertThat(result.pagination?.offset).isEqualTo(2)
		assertThat(result.pagination?.limit).isEqualTo(2)
		verify(exactly = 1) { client.fetchAllByDocType(DB_NAME, DocType.Ssm, any(), 2L, 2L) }
	}

	@Test
	suspend fun `ssm list without pagination fetches everything and counts the items it returned`() {
		val all = listOf(ssm("ssm-1"), ssm("ssm-2"))
		every { client.fetchAllByDocType(DB_NAME, DocType.Ssm, any(), null, null) } returns all

		val result = CouchdbSsmListQueryFunctionImpl(client)
			.invoke(flowOf(ssmListQuery(null)))
			.toList()
			.single()

		assertThat(result.items).isEqualTo(all)
		assertThat(result.total).isEqualTo(2)
		assertThat(result.pagination).isNull()
		// No extra count round trip when there is no page to describe.
		verify(exactly = 0) { client.countByDocType(any(), DocType.Ssm, any()) }
	}

	@Test
	suspend fun `session state list pushes the page down and keeps the ssm filter on the count`() {
		val filters = mapOf("ssm" to "CarDealership")
		val page = listOf(
			SsmSessionState(
				ssm = "CarDealership",
				session = "session-1",
				roles = emptyMap(),
				public = "{}",
				private = emptyMap(),
				origin = null,
				current = 0,
				iteration = 0,
			)
		)
		every { client.fetchAllByDocType(DB_NAME, DocType.State, filters, 1L, 5L) } returns page
		every { client.countByDocType(DB_NAME, DocType.State, filters) } returns 42

		val result = CouchdbSsmSessionStateListQueryFunctionImpl(client)
			.invoke(
				flowOf(
					CouchdbSsmSessionStateListQuery(
						pagination = OffsetPagination(offset = 5, limit = 1),
						chaincodeUri = io.komune.c2.chaincode.dsl.ChaincodeUri("chaincode:$CHANNEL_ID:$CHAINCODE_ID"),
						ssm = "CarDealership",
					)
				)
			)
			.toList()
			.single()

		assertThat(result.items).isEqualTo(page)
		assertThat(result.total).isEqualTo(42)
		verify(exactly = 1) { client.countByDocType(DB_NAME, DocType.State, filters) }
	}
}
