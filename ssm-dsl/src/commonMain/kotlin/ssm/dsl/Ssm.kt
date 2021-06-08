package ssm.dsl

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("Ssm")
interface Ssm {
	val name: String
	val transitions: Array<SsmTransitionBase>
}

@Serializable
@JsExport
@JsName("SsmBase")
data class SsmBase(
	override val name: String,
	override val transitions: Array<SsmTransitionBase>,
): Ssm