package s2.sample.did.domain.features

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import s2.sample.did.domain.DidEvent
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidInitCommand
import s2.sample.did.domain.DidState


typealias DidCreateCommandFunction = F2Function<DidCreateCommand, DidCreatedEvent>

@Serializable
@JsExport
@JsName("DidCreateCommand")
data class DidCreateCommand(
	val id: DidId,
) : DidInitCommand

@Serializable
@JsExport
@JsName("DidCreatedEvent")
data class DidCreatedEvent(
	override val id: String,
	override val type: DidState,
) : DidEvent
