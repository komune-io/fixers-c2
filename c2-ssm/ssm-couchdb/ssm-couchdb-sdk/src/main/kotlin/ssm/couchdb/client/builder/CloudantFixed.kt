package ssm.couchdb.client.builder

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ibm.cloud.cloudant.common.SdkCommon
import com.ibm.cloud.cloudant.v1.Cloudant
import com.ibm.cloud.cloudant.v1.model.ChangesResult
import com.ibm.cloud.cloudant.v1.model.PostChangesOptions
import com.ibm.cloud.sdk.core.http.RequestBuilder
import com.ibm.cloud.sdk.core.http.ServiceCall
import com.ibm.cloud.sdk.core.security.Authenticator
import com.ibm.cloud.sdk.core.util.GsonSingleton
import com.ibm.cloud.sdk.core.util.ResponseConverterUtils
import com.ibm.cloud.sdk.core.util.Validator
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName

@Suppress("LongMethod", "ComplexMethod", "CyclomaticComplexMethod", "CognitiveComplexity")
class CloudantFixed(
	serviceName: String,
	authenticator: Authenticator
) : Cloudant(serviceName, authenticator) {

	fun postChanges(
		postChangesOptions: PostChangesOptions, ssm: SsmName?, session: SessionName?
	): ServiceCall<ChangesResult> {
		Validator.notNull(
			postChangesOptions,
			"postChangesOptions cannot be null"
		)
		val pathParamsMap: MutableMap<String, String> = HashMap()
		pathParamsMap["db"] = postChangesOptions.db()
		val builder = RequestBuilder.get(
			RequestBuilder.resolveRequestUrl(
				serviceUrl, "/{db}/_changes", pathParamsMap
			)
		)
		val sdkHeaders = SdkCommon.getSdkHeaders("cloudant", "v1", "postChanges")
		for ((key, value) in sdkHeaders) {
			builder.header(key, value)
		}
		builder.header("Accept", "application/json")

		// SMART-B Use query last-event-id instead of header's one
		if (postChangesOptions.lastEventId() != null) {
			builder.query("last-event-id", postChangesOptions.lastEventId())
		}
		if (postChangesOptions.attEncodingInfo() != null) {
			builder.query("att_encoding_info", postChangesOptions.attEncodingInfo().toString())
		}
		if (postChangesOptions.attachments() != null) {
			builder.query("attachments", postChangesOptions.attachments().toString())
		}
		if (postChangesOptions.conflicts() != null) {
			builder.query("conflicts", postChangesOptions.conflicts().toString())
		}
		if (postChangesOptions.descending() != null) {
			builder.query("descending", postChangesOptions.descending().toString())
		}
		if (postChangesOptions.feed() != null) {
			builder.query("feed", postChangesOptions.feed().toString())
		}
		if (postChangesOptions.filter() != null) {
			builder.query("filter", postChangesOptions.filter().toString())
		}
		if (postChangesOptions.heartbeat() != null) {
			builder.query("heartbeat", postChangesOptions.heartbeat().toString())
		}
		if (postChangesOptions.includeDocs() != null) {
			builder.query("include_docs", postChangesOptions.includeDocs().toString())
		}
		if (postChangesOptions.limit() != null) {
			builder.query("limit", postChangesOptions.limit().toString())
		}
		if (postChangesOptions.seqInterval() != null) {
			builder.query("seq_interval", postChangesOptions.seqInterval().toString())
		}
		if (postChangesOptions.since() != null) {
			builder.query("since", postChangesOptions.since().toString())
		}
		if (postChangesOptions.style() != null) {
			builder.query("style", postChangesOptions.style().toString())
		}
		if (postChangesOptions.timeout() != null) {
			builder.query("timeout", postChangesOptions.timeout().toString())
		}
		if (postChangesOptions.view() != null) {
			builder.query("view", postChangesOptions.view().toString())
		}
		val contentJson = JsonObject()
		if (postChangesOptions.docIds() != null) {
			contentJson.add("doc_ids", GsonSingleton.getGson().toJsonTree(postChangesOptions.docIds()))
		}
		if (postChangesOptions.fields() != null) {
			contentJson.add("fields", GsonSingleton.getGson().toJsonTree(postChangesOptions.fields()))
		}
		if (postChangesOptions.selector() != null) {
			contentJson.add("selector", GsonSingleton.getGson().toJsonTree(postChangesOptions.selector()))
		}
		ssm?.let {
			builder.query("ssm", ssm)
		}
		session?.let {
			builder.query("session", session)
		}

		val responseConverter =
			ResponseConverterUtils.getValue<ChangesResult>(object : TypeToken<ChangesResult?>() {}.type)
		return createServiceCall(builder.build(), responseConverter)
	}
}
