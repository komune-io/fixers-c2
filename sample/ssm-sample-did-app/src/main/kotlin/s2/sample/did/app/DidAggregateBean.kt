package s2.sample.did.app

import f2.dsl.fnc.f2Function
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import s2.sample.did.app.config.DidS2Aggregate
import s2.sample.did.app.entity.DidEntity
import s2.sample.did.domain.DidAggregate
import s2.sample.did.domain.DidState
import s2.sample.did.domain.features.DidAddPublicKeyCommand
import s2.sample.did.domain.features.DidAddPublicKeyEvent
import s2.sample.did.domain.features.DidCreateCommand
import s2.sample.did.domain.features.DidCreatedEvent
import s2.sample.did.domain.features.DidRevokeCommandFunction
import s2.sample.did.domain.features.DidRevokePublicKeyCommandFunction

@Service
class DidAggregateBean(
	private val didS2Aggregate: DidS2Aggregate,
) : DidAggregate {

	@Bean("createDid")
	override fun createDid() = f2Function<DidCreateCommand, DidCreatedEvent> { cmd ->
		createDid(cmd)
	}

	@Bean("addPublicKey")
	override fun addPublicKey() = f2Function<DidAddPublicKeyCommand, DidAddPublicKeyEvent> { cmd ->
		addPublicKey(cmd)
	}

	@Bean("revokePublicKey")
	override fun revokePublicKey(): DidRevokePublicKeyCommandFunction = f2Function { cmd ->
		TODO("Not yet implemented")
	}

	@Bean("revoke")
	override fun revoke(): DidRevokeCommandFunction = f2Function { cmd ->
		TODO("Not yet implemented")
	}

	suspend fun createDid(cmd: DidCreateCommand): DidCreatedEvent = didS2Aggregate.createWithEvent(cmd,
		{ DidCreatedEvent(s2Id(), DidState(this.state)) }
	) {
		DidEntity(id = cmd.id, state = DidState.Created().position)
	}

	suspend fun addPublicKey(cmd: DidAddPublicKeyCommand): DidAddPublicKeyEvent = didS2Aggregate.doTransition(cmd) {
		this.publicKeys.add(cmd.publicKey)
		this.state = DidState.Activated().position
		this to DidAddPublicKeyEvent(
			id = cmd.id,
			type = DidState(this.state)
		)
	}
}
