package s2.sample.did.domain.features

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import s2.sample.did.domain.DidCommand
import s2.sample.did.domain.DidEvent
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidState

typealias DidRevokeCommandFunction = F2Function<DidRevokeCommand, DidRevokedEvent>

@Serializable
@JsExport
@JsName("DidRevokeCommand")
data class DidRevokeCommand(
	override val id: DidId,
) : DidCommand

@Serializable
@JsExport
@JsName("DidRevokedEvent")
data class DidRevokedEvent(
	override val id: DidId,
	override val type: DidState,
) : DidEvent
