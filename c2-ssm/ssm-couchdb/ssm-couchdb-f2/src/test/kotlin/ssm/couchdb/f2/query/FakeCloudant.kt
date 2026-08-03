package ssm.couchdb.f2.query

import com.ibm.cloud.cloudant.v1.model.DatabaseInformation
import com.ibm.cloud.cloudant.v1.model.FindResult
import com.ibm.cloud.cloudant.v1.model.GetDatabaseInformationOptions
import com.ibm.cloud.cloudant.v1.model.PostFindOptions
import com.ibm.cloud.sdk.core.http.Response
import com.ibm.cloud.sdk.core.http.ServiceCall
import com.ibm.cloud.sdk.core.http.ServiceCallback
import com.ibm.cloud.sdk.core.security.NoAuthAuthenticator
import com.ibm.cloud.sdk.core.util.GsonSingleton
import io.reactivex.Single
import okhttp3.Protocol
import okhttp3.Request
import ssm.couchdb.client.CouchdbSsmClient
import ssm.couchdb.client.builder.CloudantFixed
import ssm.sdk.json.JSONConverterObjectMapper

/**
 * In-memory CouchDB double: responses are canned JSON payloads,
 * no network connection is ever opened.
 */
class FakeCloudant : CloudantFixed("fake-couchdb", NoAuthAuthenticator()) {

	var allDbsResult: List<String> = emptyList()
	val findResultsByDb = mutableMapOf<String, String>()
	var databaseInformationJson: String? = null
	var lastFindOptions: PostFindOptions? = null

	init {
		serviceUrl = "http://localhost:5984"
	}

	override fun getAllDbs(): ServiceCall<MutableList<String>> = stub(allDbsResult.toMutableList())

	override fun postFind(postFindOptions: PostFindOptions): ServiceCall<FindResult> {
		lastFindOptions = postFindOptions
		val json = findResultsByDb[postFindOptions.db()] ?: """{"docs":[]}"""
		return stub(GsonSingleton.getGson().fromJson(json, FindResult::class.java))
	}

	override fun getDatabaseInformation(
		getDatabaseInformationOptions: GetDatabaseInformationOptions,
	): ServiceCall<DatabaseInformation> {
		val json = databaseInformationJson
			?: throw IllegalStateException("Database ${getDatabaseInformationOptions.db()} not found")
		return stub(GsonSingleton.getGson().fromJson(json, DatabaseInformation::class.java))
	}

	private fun <T> stub(result: T): ServiceCall<T> = object : ServiceCall<T> {
		override fun addHeader(name: String, value: String): ServiceCall<T> = this
		override fun execute(): Response<T> = Response(result, okResponse())
		override fun enqueue(callback: ServiceCallback<T>) = callback.onResponse(execute())
		override fun reactiveRequest(): Single<Response<T>> = Single.just(execute())
		override fun cancel() = Unit
	}

	private fun okResponse(): okhttp3.Response = okhttp3.Response.Builder()
		.request(Request.Builder().url("http://localhost:5984/").build())
		.protocol(Protocol.HTTP_1_1)
		.code(200)
		.message("OK")
		.build()
}

fun fakeCouchdbClient(): Pair<CouchdbSsmClient, FakeCloudant> {
	val cloudant = FakeCloudant()
	return CouchdbSsmClient(cloudant, JSONConverterObjectMapper()) to cloudant
}
