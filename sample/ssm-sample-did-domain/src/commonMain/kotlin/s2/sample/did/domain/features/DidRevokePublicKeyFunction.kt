package s2.sample.did.domain.features

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import s2.sample.did.domain.DidCommand
import s2.sample.did.domain.DidEvent
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidState

typealias DidRevokePublicKeyCommandFunction = F2Function<DidRevokePublicKeyCommand, DidRevokedPublicKeyEvent>

@Serializable
@JsExport
@JsName("DidRevokePublicKeyCommand")
data class DidRevokePublicKeyCommand(
	override val id: DidId,
) : DidCommand

@Serializable
@JsExport
@JsName("DidRevokedPublicKeyEvent")
data class DidRevokedPublicKeyEvent(
	override val type: DidState,
	override val id: DidId,
) : DidEvent
