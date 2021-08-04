package ssm.chaincode.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.SsmAgent
import ssm.chaincode.dsl.SsmCommandDTO
import kotlin.js.JsExport
import kotlin.js.JsName

typealias SsmGetAdminFunction = F2Function<SsmGetAdminQuery, SsmGetAdminResult>

@JsExport
@Serializable
@JsName("SsmGetAdminQuery")
class SsmGetAdminQuery(
		override val baseUrl: String,
		override val channelId: String?,
		override val chaincodeId: String?,
		override val bearerToken: String? = null,
		val name: String
): SsmCommandDTO

@JsExport
@Serializable
@JsName("SsmGetAdminResult")
class SsmGetAdminResult(
	val admin: SsmAgent?
): Event