package s2.sample.did.domain.client

import f2.client.F2Client
import f2.client.function
import f2.client.ktor.F2ClientBuilder
import f2.dsl.fnc.F2SupplierSingle
import kotlin.js.JsExport
import kotlin.js.JsName
import s2.sample.did.domain.DidAggregate
import s2.sample.did.domain.features.DidAddPublicKeyCommandFunction
import s2.sample.did.domain.features.DidCreateCommandFunction
import s2.sample.did.domain.features.DidRevokeCommandFunction
import s2.sample.did.domain.features.DidRevokePublicKeyCommandFunction

fun didClient(
	urlBase: String,
): F2SupplierSingle<DIDFunctionClient> {
	return F2SupplierSingle {
		val s2Client = F2ClientBuilder.get(urlBase)
		DIDFunctionClient(s2Client)
	}
}

@JsName("DIDFunctionClient")
@JsExport
open class DIDFunctionClient(private val client: F2Client) : DidAggregate {
	override fun createDid(): DidCreateCommandFunction = client.function("createDid")
	override fun addPublicKey(): DidAddPublicKeyCommandFunction = client.function("addPublicKey")
	override fun revokePublicKey(): DidRevokePublicKeyCommandFunction = client.function("revokePublicKey")
	override fun revoke(): DidRevokeCommandFunction = client.function("revoke")
}
