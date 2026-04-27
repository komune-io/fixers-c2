package s2.sample.did.domain.features

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import s2.sample.did.domain.DidCommand
import s2.sample.did.domain.DidEvent
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidState

typealias DidAddPublicKeyCommandFunction = F2Function<DidAddPublicKeyCommand, DidAddPublicKeyEvent>

@Serializable
@JsExport
@JsName("DidAddPublicKeyCommand")
data class DidAddPublicKeyCommand(
	override val id: DidId,
	val publicKey: String,
) : DidCommand

@Serializable
@JsExport
@JsName("DidAddPublicKeyEvent")
data class DidAddPublicKeyEvent(
	override val id: DidId,
	override val type: DidState,
) : DidEvent
