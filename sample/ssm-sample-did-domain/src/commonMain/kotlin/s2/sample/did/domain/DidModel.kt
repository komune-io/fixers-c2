package s2.sample.did.domain

import kotlinx.serialization.Serializable
import s2.dsl.automate.model.WithS2IdAndStatus

@Serializable
data class DidModel(
	val id: DidId,
	val state: DidState,
) : WithS2IdAndStatus<DidId, DidState> {
	override fun s2Id() = id
	override fun s2State() = state
}
