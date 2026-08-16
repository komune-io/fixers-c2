@file:JvmName("PrivateMessageUtils")

package ssm.sdk.sign.extention

import java.security.PrivateKey
import java.security.PublicKey
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.WithPrivate
import ssm.sdk.sign.crypto.RSACipher
import ssm.sdk.sign.model.Signer

@Throws(Exception::class)
fun SsmContext.addPrivateMessage(value: String, agent: Agent): SsmContext {
	return addPrivateMessage(value, agent.name, agent.getPubAsKey())
}

@Throws(Exception::class)
fun SsmContext.addPrivateMessage(value: String, name: String, publicKey: PublicKey): SsmContext {
	val newMap = addPrivate(value, publicKey, name)
	return this.copy(private = newMap)
}

@Throws(Exception::class)
fun SsmSession.addPrivateMessage(value: String, name: String, publicKey: PublicKey): SsmSession {
	val newPrivate = addPrivate(value, publicKey, name)
	return SsmSession(ssm, session, roles, public, newPrivate)
}

@Throws(Exception::class)
fun SsmSessionState.addPrivateMessage(value: String, name: String, publicKey: PublicKey): SsmSessionState {
	val newPrivate = addPrivate(value, publicKey, name)
	return copy(private = newPrivate)
}

private fun WithPrivate.addPrivate(value: String, publicKey: PublicKey, name: String): Map<String, String> {
	val encrypted = RSACipher.encrypt(value.toByteArray(), publicKey)
	return (private ?: hashMapOf()) + mapOf(name to encrypted)
}

@Throws(Exception::class)
fun WithPrivate.getPrivateMessage(signer: Signer): String? {
	return getPrivateMessage(signer.name, signer.pair.private)
}

@Throws(Exception::class)
fun WithPrivate.getPrivateMessage(name: String, privateKey: PrivateKey): String? {
	val value = private?.get(name) ?: return null
	return RSACipher.decrypt(value, privateKey)
}
