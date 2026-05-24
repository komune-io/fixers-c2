package ssm.sdk.core.invoke.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import org.assertj.core.api.Assertions
import org.assertj.core.util.Lists
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.dsl.buildArgs
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.crypto.KeyPairReader.loadKeyPair
import ssm.sdk.sign.crypto.Sha256RSASigner.rsaSignAsB64
import ssm.sdk.sign.model.SignerUser

class CreateCommandTest {
	/**
	 * "name":"Car dealership",
	 * "transitions":[
	 * {
	 * "from":0,
	 * "to":1,
	 * "role":"Seller",
	 * "action":"Sell"
	 * },
	 * {
	 * "from":1,
	 * "to":2,
	 * "role":"Buyer",
	 * "action":"Buy"
	 * }
	 * ]
	 */
	@Test
	@Throws(Exception::class)
	fun testExecute() {
		val  chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
		val adamPair = loadKeyPair("command/adam")
		val signerUser = SignerUser("adam", adamPair)
		val signer = SsmCmdSignerSha256RSASigner(
			signerUser
		)
		val sell = SsmTransition(0, 1, "Seller", "Sell")
		val buy = SsmTransition(1, 2, "Buyer", "Buy")
		val ssm = Ssm("dealership", Lists.newArrayList(sell, buy))

		val (fcn, args) = CreateCmd(ssm).invoke(chaincodeUri, signerUser.name, signer).buildArgs()

		args.forEach { s: String? -> println(s) }
		/**
		 * {
		 * "InvokeArgs": [
		 * "create",
		 * "{\"name\":\"Car dealership\",\"transitions\":[{\"from\":0,\"to\":1,\"role\":\"Seller\",\"action\":\"Sell\"},{\"from\":1,\"to\":2,\"role\":\"Buyer\",\"action\":\"Buy\"}]}",
		 * "adam",
		 * "HUYPNHkgCfB+yr7TeYpi1dcU8me+MzPqFxtxJWBeIunBo/KHuG7/bS32MakwwDf7ehyIWDuXF42b/IT9RofKLU6P5DwpadDxE6cj1qlcIgRd1K015D9wvKFdJW9SfYTJhINwuitFhus/eNLcGb+CdyoyD0GRrYRONJ8C6/Hop2PwyCZ6v5aya+XxEoh+2EjPkdeDn0VbdXR5wGP7emI4R9ZhAHwp3ebHV139OdSvvGobllN9hUZdKBkF2nYinti/YfrBI9mfY4svPCg1zZfK0hfegAa8Rekysno/2+d9jkJMwCveTzclMpSFGlVO3mRr4yWQOIEre7VpaxfGx8zdow=="
		 * ]
		 * }
		 */
		@Suppress("MaxLineLength")
		val expectedJson =
			"{\"name\":\"dealership\",\"transitions\":[{\"from\":0,\"to\":1,\"role\":\"Seller\",\"action\":\"Sell\"},{\"from\":1,\"to\":2,\"role\":\"Buyer\",\"action\":\"Buy\"}]}"
		Assertions.assertThat(fcn).isEqualTo("create")
		Assertions.assertThat(args)
			.isNotEmpty
			.containsExactly(
				expectedJson,
				"adam",
				rsaSignAsB64(expectedJson, signerUser.pair.private)
			)
	}
}
