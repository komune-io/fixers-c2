package s2.sample.did.app.entity

import java.util.UUID
import s2.dsl.automate.model.WithS2IdAndStatus
import s2.sample.did.domain.DidId
import s2.sample.did.domain.DidState

data class DidEntity(
	val id: DidId = UUID.randomUUID().toString(),
	val publicKeys: MutableList<String> = mutableListOf(),
	var state: Int,
) : WithS2IdAndStatus<DidId, DidState> {
	override fun s2Id() = id
	override fun s2State() = DidState(state)
}
