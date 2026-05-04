package s2.sample.did.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.sample.did.domain.features.DidAddPublicKeyCommand
import s2.sample.did.domain.features.DidCreateCommand
import s2.sample.did.domain.features.DidRevokeCommand
import s2.sample.did.domain.features.DidRevokePublicKeyCommand

class DidS2Test {

	@Test
	fun `DidRevokeCommand transitions from Activated to Revoked`() {
		val transition = didS2.transitions.first { it.action.name == DidRevokeCommand::class.simpleName }
		assertThat(transition.from!!.position).isEqualTo(DidState.Activated().position)
		assertThat(transition.to.position).isEqualTo(DidState.Revoked().position)
	}

	@Test
	fun `DidRevokePublicKeyCommand transitions from Activated to Activated`() {
		val transition = didS2.transitions.first { it.action.name == DidRevokePublicKeyCommand::class.simpleName }
		assertThat(transition.from!!.position).isEqualTo(DidState.Activated().position)
		assertThat(transition.to.position).isEqualTo(DidState.Activated().position)
	}

	@Test
	fun `DidCreateCommand is an init transition with no from state`() {
		val transition = didS2.transitions.first { it.action.name == DidCreateCommand::class.simpleName }
		assertThat(transition.from).isNull()
		assertThat(transition.to.position).isEqualTo(DidState.Created().position)
	}

	@Test
	fun `DidAddPublicKeyCommand transitions from Created to Activated`() {
		val transition = didS2.transitions.first { it.action.name == DidAddPublicKeyCommand::class.simpleName }
		assertThat(transition.from!!.position).isEqualTo(DidState.Created().position)
		assertThat(transition.to.position).isEqualTo(DidState.Activated().position)
	}

	@Test
	fun `automate has exactly 4 transitions`() {
		assertThat(didS2.transitions).hasSize(4)
	}
}
