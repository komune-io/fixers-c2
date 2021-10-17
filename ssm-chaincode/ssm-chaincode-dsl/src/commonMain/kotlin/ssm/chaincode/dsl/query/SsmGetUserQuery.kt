package ssm.chaincode.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.SsmAgent
import ssm.chaincode.dsl.SsmQueryDTO

typealias SsmGetUserFunction = F2Function<SsmGetUserQuery, SsmGetUserResult>

@Serializable
@JsExport
@JsName("SsmGetUserQuery")
class SsmGetUserQuery(
	override val bearerToken: String? = null,
	val name: String,
) : SsmQueryDTO

@Serializable
@JsExport
@JsName("SsmGetUserResult")
class SsmGetUserResult(
	val user: SsmAgent?,
) : Event
