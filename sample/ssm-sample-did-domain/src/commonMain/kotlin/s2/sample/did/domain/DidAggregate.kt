package s2.sample.did.domain

import s2.sample.did.domain.features.DidAddPublicKeyCommandFunction
import s2.sample.did.domain.features.DidCreateCommandFunction
import s2.sample.did.domain.features.DidRevokeCommandFunction
import s2.sample.did.domain.features.DidRevokePublicKeyCommandFunction

interface DidAggregate {
	fun createDid(): DidCreateCommandFunction
	fun addPublicKey(): DidAddPublicKeyCommandFunction
	fun revokePublicKey(): DidRevokePublicKeyCommandFunction
	fun revoke(): DidRevokeCommandFunction
}
