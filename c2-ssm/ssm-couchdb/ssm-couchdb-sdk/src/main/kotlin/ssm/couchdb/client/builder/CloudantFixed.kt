package ssm.couchdb.client.builder

import com.google.gson.reflect.TypeToken
import com.ibm.cloud.cloudant.common.SdkCommon
import com.ibm.cloud.cloudant.v1.Cloudant
import com.ibm.cloud.cloudant.v1.model.ChangesResult
import com.ibm.cloud.cloudant.v1.model.PostChangesOptions
import com.ibm.cloud.sdk.core.http.RequestBuilder
import com.ibm.cloud.sdk.core.http.ServiceCall
import com.ibm.cloud.sdk.core.security.Authenticator
import com.ibm.cloud.sdk.core.util.ResponseConverterUtils
import com.ibm.cloud.sdk.core.util.Validator
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName

open class CloudantFixed(
	serviceName: String,
	authenticator: Authenticator
) : Cloudant(serviceName, authenticator) {

	/**
	 * Custom `_changes` request that, unlike [Cloudant.postChanges], passes `last-event-id`,
	 * `ssm` and `session` as query parameters so they reach the `ssm_changes_filter`
	 * design-document filter.
	 *
	 * The request is issued as a GET without a body: the body-only options of
	 * [PostChangesOptions] (`docIds`, `fields`, `selector`) are intentionally not supported
	 * and are ignored. They would require `filter=_doc_ids`/`_selector`, which conflicts
	 * with the custom design-document filter this client relies on.
	 */
	fun postChanges(
		postChangesOptions: PostChangesOptions, ssm: SsmName?, session: SessionName?
	): ServiceCall<ChangesResult> {
		Validator.notNull(
			postChangesOptions,
			"postChangesOptions cannot be null"
		)
		val pathParamsMap: Map<String, String> = mapOf("db" to postChangesOptions.db())
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
		builder.applyQueryParameters(postChangesOptions, ssm, session)

		val responseConverter =
			ResponseConverterUtils.getValue<ChangesResult>(object : TypeToken<ChangesResult?>() {}.type)
		return createServiceCall(builder.build(), responseConverter)
	}

	private fun RequestBuilder.applyQueryParameters(
		options: PostChangesOptions,
		ssm: SsmName?,
		session: SessionName?
	) {
		mapOf(
			"last-event-id" to options.lastEventId(),
			"att_encoding_info" to options.attEncodingInfo(),
			"attachments" to options.attachments(),
			"conflicts" to options.conflicts(),
			"descending" to options.descending(),
			"feed" to options.feed(),
			"filter" to options.filter(),
			"heartbeat" to options.heartbeat(),
			"include_docs" to options.includeDocs(),
			"limit" to options.limit(),
			"seq_interval" to options.seqInterval(),
			"since" to options.since(),
			"style" to options.style(),
			"timeout" to options.timeout(),
			"view" to options.view(),
			"ssm" to ssm,
			"session" to session
		).forEach { (name, value) ->
			if (value != null) {
				query(name, value.toString())
			}
		}
	}
}
