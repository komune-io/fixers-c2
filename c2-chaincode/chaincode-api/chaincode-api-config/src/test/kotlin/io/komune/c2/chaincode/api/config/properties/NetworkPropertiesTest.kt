package io.komune.c2.chaincode.api.config.properties

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class NetworkPropertiesTest {

	@TempDir
	lateinit var cryptoDir: File

	private fun peer() = PeerProperties(
		requests = "grpcs://peer0:7051",
		events = "grpcs://peer0:7053",
		serverHostname = "peer0.bc-coop.bclan",
		tlsCacerts = "tls/ca.crt",
		keystore = "msp/keystore",
		signcerts = "msp/signcerts",
	)

	private fun ca() = CaProperties(
		name = "ca.bc-coop.bclan",
		url = "https://ca:7054",
		tlsCacerts = "tls/ca.crt",
	)

	private fun orderer() = OrdererProperties(
		url = "grpcs://orderer:7050",
		serverHostname = "orderer.bclan",
		tlsCacerts = "tls/ca.crt",
	)

	private fun network() = NetworkProperties(
		orderer = orderer(),
		organisations = mapOf(
			"bclan" to OrganisationProperties(
				name = "bclan",
				mspid = "BclanMSP",
				ca = ca(),
				peers = mapOf("peer0" to peer()),
			)
		),
	)

	@Test
	fun `network properties expose orderer and organisations`() {
		val network = network()

		assertThat(network.orderer.url).isEqualTo("grpcs://orderer:7050")
		assertThat(network.orderer.serverHostname).isEqualTo("orderer.bclan")
		val organisation = network.organisations.getValue("bclan")
		assertThat(organisation.name).isEqualTo("bclan")
		assertThat(organisation.mspid).isEqualTo("BclanMSP")
		assertThat(organisation.ca.name).isEqualTo("ca.bc-coop.bclan")
		assertThat(organisation.ca.url).isEqualTo("https://ca:7054")
		val peer = organisation.peers.getValue("peer0")
		assertThat(peer.requests).isEqualTo("grpcs://peer0:7051")
		assertThat(peer.events).isEqualTo("grpcs://peer0:7053")
		assertThat(peer.serverHostname).isEqualTo("peer0.bc-coop.bclan")
	}

	@Test
	fun `tls cacerts keystore and signcerts resolve against the crypto base`() {
		val peer = peer()
		val base = "file:${cryptoDir.absolutePath}"

		assertThat(peer.getTlsCacertsAsUrl(base).toString()).isEqualTo("$base/tls/ca.crt")
		assertThat(peer.getKeystoreAsUrl(base).toString()).isEqualTo("$base/msp/keystore")
		assertThat(peer.getSigncertsAsUrl(base).toString()).isEqualTo("$base/msp/signcerts")
	}

	@Test
	fun `peer tls properties allow all host names and point to the pem file`() {
		val peer = peer()
		val base = "file:${cryptoDir.absolutePath}/"

		val properties = peer.getPeerTlsProperties(base)

		assertThat(properties.getProperty("allowAllHostNames")).isEqualTo("true")
		assertThat(properties.getProperty("pemFile")).isEqualTo("${cryptoDir.absolutePath}/tls/ca.crt")
	}
}
