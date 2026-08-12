@file:Suppress("MaxLineLength")

package ssm.sdk.core.invoke.builder

import io.komune.c2.chaincode.dsl.ChaincodeUri
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.core.invoke.command.CreateCmd
import ssm.sdk.core.invoke.command.PerformCmd
import ssm.sdk.core.invoke.command.RegisterCmd
import ssm.sdk.core.invoke.command.StartCmd
import ssm.sdk.sign.extention.loadFromFile

/**
 * Golden payloads for the canonical form that gets signed and sent on-chain.
 *
 * The bytes covered here are produced by Jackson (`JsonUtils.toJson`, NON_NULL inclusion) and are
 * verified chain-side against the agent signature. Any property reorder, rename or inclusion change
 * silently breaks that verification, so these expectations are intentionally exact strings: they
 * lock the current wire form and must only be updated together with the chaincode verifier.
 */
class SignedPayloadGoldenTest {

	companion object {
		private val CHAINCODE_URI = ChaincodeUri("chaincode:sandbox:ssm")
		private const val AGENT_NAME = "adam"
	}

	@Test
	fun `create payload is stable`() {
		val ssm = Ssm(
			name = "dealership",
			transitions = listOf(
				SsmTransition(0, 1, "Seller", "Sell"),
				SsmTransition(1, 2, "Buyer", "Buy")
			)
		)

		val cmd = CreateCmd(ssm).commandToSign(CHAINCODE_URI, AGENT_NAME)

		val expectedJson =
			"""{"name":"dealership","transitions":[{"from":0,"to":1,"role":"Seller","action":"Sell"},{"from":1,"to":2,"role":"Buyer","action":"Buy"}]}"""
		Assertions.assertThat(cmd.json).isEqualTo(expectedJson)
		Assertions.assertThat(cmd.valueToSign).isEqualTo(expectedJson)
		Assertions.assertThat(cmd.performAction).isNull()
	}

	@Test
	fun `register payload is stable`() {
		val agent = Agent.loadFromFile("vivi", "command/vivi")

		val cmd = RegisterCmd(agent).commandToSign(CHAINCODE_URI, AGENT_NAME)

		val expectedJson =
			"""{"name":"vivi","pub":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs2oFqOlrdpz/fEi5rQfEFWeWTeSXSLaaEwQAYof+EIQTYlvQ+1uk//dBFn+bPcp+BSdzgkra4jd0qsImVMgnrWIDUhs3vl2Wi9TgAQHXT/DtIbvlj+ZdPFTUzd3vb+8NR4i4ha8Yg9bbd5noaf3f40aJ1CY+huRV0/ElOFI5/hM00rZdxiFNcQ9NiA++osUzb4OZ5TqnePmwDpnI7qbE9mTOlbJju9JfmnppZv2HRkWRsdCPjKm+mKv5O9xR+Np5bSMTGqrVH0eyMleHrALEojDdfLt2FTf+ZiCCVKulV5jbMpKf3Qt7891vC5/QyDrtbEz7aPhU4FT1W2ks6rOLcwIDAQAB"}"""
		Assertions.assertThat(cmd.json).isEqualTo(expectedJson)
		Assertions.assertThat(cmd.valueToSign).isEqualTo(expectedJson)
	}

	@Test
	fun `start payload is stable`() {
		val session = SsmSession(
			ssm = "Car dealership",
			session = "deal20181201",
			roles = mapOf("chuck" to "Buyer", "sarah" to "Seller"),
			public = "Used car for 100 dollars.",
			private = null
		)

		val cmd = StartCmd(session).commandToSign(CHAINCODE_URI, AGENT_NAME)

		val expectedJson =
			"""{"ssm":"Car dealership","session":"deal20181201","roles":{"chuck":"Buyer","sarah":"Seller"},"public":"Used car for 100 dollars."}"""
		Assertions.assertThat(cmd.json).isEqualTo(expectedJson)
		Assertions.assertThat(cmd.valueToSign).isEqualTo(expectedJson)
	}

	@Test
	fun `start payload with private data is stable`() {
		val session = SsmSession(
			ssm = "Car dealership",
			session = "deal20181201",
			roles = mapOf("chuck" to "Buyer", "sarah" to "Seller"),
			public = "Used car for 100 dollars.",
			private = mapOf("chuck" to "message")
		)

		val cmd = StartCmd(session).commandToSign(CHAINCODE_URI, AGENT_NAME)

		Assertions.assertThat(cmd.json).isEqualTo(
			"""{"ssm":"Car dealership","session":"deal20181201","roles":{"chuck":"Buyer","sarah":"Seller"},"public":"Used car for 100 dollars.","private":{"chuck":"message"}}"""
		)
	}

	@Test
	fun `perform payload is stable and prefixed by the action`() {
		val context = SsmContext(
			session = "deal20181201",
			public = "100 dollars 1978 Camaro",
			iteration = 0,
			private = null
		)

		val cmd = PerformCmd("Sell", context).commandToSign(CHAINCODE_URI, AGENT_NAME)

		val expectedJson = """{"session":"deal20181201","public":"100 dollars 1978 Camaro","iteration":0}"""
		Assertions.assertThat(cmd.json).isEqualTo(expectedJson)
		Assertions.assertThat(cmd.performAction).isEqualTo("Sell")
		Assertions.assertThat(cmd.valueToSign).isEqualTo("Sell$expectedJson")
	}

	@Test
	fun `perform payload with private data is stable`() {
		val context = SsmContext(
			session = "deal20181201",
			public = "100 dollars 1978 Camaro",
			iteration = 0,
			private = mapOf("vivi" to "message")
		)

		val cmd = PerformCmd("Sell", context).commandToSign(CHAINCODE_URI, AGENT_NAME)

		val expectedJson =
			"""{"session":"deal20181201","public":"100 dollars 1978 Camaro","iteration":0,"private":{"vivi":"message"}}"""
		Assertions.assertThat(cmd.json).isEqualTo(expectedJson)
		Assertions.assertThat(cmd.valueToSign).isEqualTo("Sell$expectedJson")
	}
}
