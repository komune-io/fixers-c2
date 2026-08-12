package ssm.couchdb.client

import com.ibm.cloud.cloudant.v1.model.ChangesResult
import com.ibm.cloud.cloudant.v1.model.DatabaseInformation
import com.ibm.cloud.cloudant.v1.model.DesignDocument
import com.ibm.cloud.cloudant.v1.model.Document
import com.ibm.cloud.cloudant.v1.model.FindResult
import com.ibm.cloud.cloudant.v1.model.GetDatabaseInformationOptions
import com.ibm.cloud.cloudant.v1.model.GetDesignDocumentOptions
import com.ibm.cloud.cloudant.v1.model.PostChangesOptions
import com.ibm.cloud.cloudant.v1.model.PostFindOptions
import com.ibm.cloud.cloudant.v1.model.PostViewOptions
import com.ibm.cloud.cloudant.v1.model.PutDesignDocumentOptions
import com.ibm.cloud.sdk.core.http.Response
import com.ibm.cloud.sdk.core.service.exception.NotFoundException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.couchdb.client.builder.CloudantFixed
import ssm.couchdb.client.builder.SsmCouchDbClientBuilder
import ssm.couchdb.dsl.model.ChangeEventId
import ssm.couchdb.dsl.model.DatabaseName
import ssm.couchdb.dsl.model.DocType
import ssm.sdk.json.JSONConverter


/**
 * The Cloudant SDK is blocking, so every call is dispatched off the calling thread.
 * [dispatcher] defaults to [Dispatchers.IO] and is injectable so tests can supply a
 * deterministic dispatcher.
 */
class CouchdbSsmClient(
	val cloudant: CloudantFixed,
	private val converter: JSONConverter,
	private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

	companion object {
		const val COUNTING_VIEW = "indexType"
		const val FABRIC_COUNTING_DOC = "indexTypeDoc"
		const val SSM_CHANGES_FILTER = "ssm_changes_filter"
		const val SSM_CHANGES_FILTER_FNC = """
			function (doc, req) { 
			  if (doc.docType && doc.docType == 'ssm' && doc.name && doc.name == req.query.ssm) {
			    return true; 
			  } else if (doc.docType && doc.docType == 'state' && doc.ssm && doc.ssm == req.query.ssm) {
			    if(req.query.session) {
				  return  doc.session == req.query.session
				} else {
				  return true;
				} 
			  } else {
			    return false;
			  }
			}"""

		fun builder(): SsmCouchDbClientBuilder {
			return SsmCouchDbClientBuilder()
		}
	}

	suspend fun <T : Any> fetchAllByDocType(
		dbName: String,
		docType: DocType<T>,
		filters: Map<String, String> = emptyMap(),
	): List<T> = withContext(dispatcher) {
		val selector = mapOf(
			"docType" to mapOf("\$eq" to docType.name)
		).plus(filters)

		val findOptions = PostFindOptions.Builder()
			.db(dbName)
			.selector(selector)
			.limit(Long.MAX_VALUE)
			.build()

		val result: Response<FindResult> = cloudant.postFind(findOptions).execute()

		result.result.docs.mapNotNull { document ->
			converter.toObject(docType.clazz.java, document.toString())
		}
	}

	suspend fun fetchAll(
		dbName: String,
	): List<Document> = withContext(dispatcher) {
		val findOptions = PostFindOptions.Builder()
			.db(dbName)
			.selector(emptyMap())
			.limit(Long.MAX_VALUE)
			.build()

		val result: Response<FindResult> = cloudant.postFind(findOptions).execute()

		result.result.docs
	}

	suspend fun <T : Any> fetchOneByDocTypeAndName(
		dbName: String,
		docType: DocType<T>,
		name: String,
	): T? = withContext(dispatcher) {
		val selector = mapOf(
			"docType" to mapOf("\$eq" to docType.name),
			"name" to name
		)

		val findOptions = PostFindOptions.Builder()
			.db(dbName)
			.selector(selector)
			.limit(Long.MAX_VALUE)
			.build()

		val result: Response<FindResult> = cloudant.postFind(findOptions).execute()

		result.result.docs.firstOrNull()?.let { document ->
			converter.toObject(docType.clazz.java, document.toString())
		}
	}

	suspend fun getDatabases(): List<String> = withContext(dispatcher) {
		cloudant.allDbs.execute().result
	}

	suspend fun getDatabase(dbName: String): DatabaseInformation = withContext(dispatcher) {
		val query = GetDatabaseInformationOptions.Builder().db(dbName).build()
		cloudant.getDatabaseInformation(query).execute().result
	}

	suspend fun getSsmChanges(
		dbName: DatabaseName,
		ssmName: SsmName,
		sessionName: SessionName?,
		lastEventId: ChangeEventId? = null,
		limit: Long? = null
	): ChangesResult {
		installSsmChangesFilter(dbName)
		val query = PostChangesOptions.Builder()
			.db(dbName)
			.lastEventId(lastEventId)
			.includeDocs(true)
			.filter("filters/$SSM_CHANGES_FILTER")

		limit?.let {
			query.limit(limit)
		}

		return withContext(dispatcher) {
			cloudant.postChanges(query.build(), ssmName, sessionName).execute().result
		}
	}

	@Suppress("SwallowedException")
	suspend fun installSsmChangesFilter(dbName: DatabaseName): Boolean = withContext(dispatcher) {
		suspendCoroutine { continuation ->
			try {
				cloudant.getDesignDocument(
					GetDesignDocumentOptions.Builder()
						.db(dbName)
						.ddoc("filters")
						.build()
				).execute().result
				continuation.resume(false)
			} catch (e: NotFoundException) {
				val stateSsmNameFilter = PutDesignDocumentOptions.Builder()
					.db(dbName)
					.ddoc("filters")
					.designDocument(
						DesignDocument.Builder().filters(
							mapOf(
								SSM_CHANGES_FILTER to SSM_CHANGES_FILTER_FNC
							)
						).build()
					).build()

				cloudant.putDesignDocument(stateSsmNameFilter).reactiveRequest().doAfterSuccess {
					continuation.resume(true)
				}.subscribe()
			}
		}
	}

	suspend fun <T : Any> getCount(dbName: String, docType: DocType<T>): Int = withContext(dispatcher) {
		val query = PostViewOptions.Builder()
			.db(dbName)
			.ddoc(FABRIC_COUNTING_DOC)
			.view(COUNTING_VIEW)
			.groupLevel(1)
			.key(arrayOf(docType.name))
			.build()
		(cloudant.postView(query).execute().result.rows.firstOrNull()?.value as Number?)?.toInt() ?: 0
	}
}
