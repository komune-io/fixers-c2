package s2.sample.did.domain.features

import f2.dsl.fnc.F2Function
import kotlinx.serialization.Serializable
import s2.sample.did.domain.DidEvent
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidInitCommand
import s2.sample.did.domain.DidState

typealias DidCreateCommandFunction = F2Function<DidCreateCommand, DidCreatedEvent>

@Serializable
data class DidCreateCommand(
    val id: DidId,
) : DidInitCommand

@Serializable
data class DidCreatedEvent(
    override val id: String,
    override val type: DidState,
) : DidEvent
