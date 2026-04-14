package s2.sample.did.domain

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import s2.dsl.automate.S2Command
import s2.dsl.automate.S2Event
import s2.dsl.automate.S2InitCommand
import s2.dsl.automate.S2Role
import s2.dsl.automate.S2State
import s2.dsl.automate.builder.s2
import s2.sample.did.domain.features.DidAddPublicKeyCommand
import s2.sample.did.domain.features.DidCreateCommand
import s2.sample.did.domain.features.DidRevokeCommand
import s2.sample.did.domain.features.DidRevokePublicKeyCommand

typealias DidId = String

@JsExport
@JsName("didS2")
val didS2 = s2 {
	name = "DidS2"
	init<DidCreateCommand> {
		to = DidState.Created()
		role = DidRole.Admin()
	}
	transaction<DidAddPublicKeyCommand> {
		from = DidState.Created()
		to = DidState.Activated()
		role = DidRole.Owner()
	}
	transaction<DidRevokeCommand> {
		from = DidState.Activated()
		to = DidState.Activated()
		role = DidRole.Owner()
	}
	transaction<DidRevokePublicKeyCommand> {
		from = DidState.Activated()
		to = DidState.Revoked()
		role = DidRole.Owner()
	}
}

@Serializable
@JsExport
@JsName("DidRole")
sealed class DidRole : S2Role {
	class Admin : DidRole()
	class Owner : DidRole()

	override fun toString(): String {
		return this::class.simpleName!!
	}
}

@Serializable
@JsExport
@JsName("DidState")
open class DidState(
	override val position: Int,
) : S2State {
	@Serializable
	class Created : DidState(0)

	@Serializable
	class Activated : DidState(1)

	@Serializable
	class Revoked : DidState(2)
}

@JsExport
@JsName("DidInitCommand")
interface DidInitCommand : S2InitCommand

@JsExport
@JsName("DidCommand")
interface DidCommand : S2Command<DidId>

@JsExport
@JsName("DidEvent")
interface DidEvent : S2Event<DidState, DidId>
