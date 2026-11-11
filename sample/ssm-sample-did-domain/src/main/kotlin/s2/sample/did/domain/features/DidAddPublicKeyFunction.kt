package s2.sample.did.domain.features

import f2.dsl.fnc.F2Function
import kotlinx.serialization.Serializable
import s2.sample.did.domain.DidCommand
import s2.sample.did.domain.DidEvent
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidState

typealias DidAddPublicKeyCommandFunction = F2Function<DidAddPublicKeyCommand, DidAddPublicKeyEvent>

@Serializable
data class DidAddPublicKeyCommand(
    override val id: DidId,
    val publicKey: String,
) : DidCommand

@Serializable
data class DidAddPublicKeyEvent(
    override val id: DidId,
    override val type: DidState,
) : DidEvent
