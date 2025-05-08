package ssm.sdk.core.invoke.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.dsl.buildArgs
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.crypto.Sha256RSASigner.rsaSignAsB64
import ssm.sdk.sign.model.SignerUser

class StartCommandTest {
	@Test
	@Throws(Exception::class)
	fun test_execute() {
		val  chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
		//    "ssm":"Car dealership",
		//    "session":"deal20181201",
		//    "public":"Used car for 100 dollars.",
		//    "roles":{
		//      "chuck":"Buyer",
		//       "sarah":"Seller"
		//    }
		val roles: Map<String, String> = mapOf("chuck" to "Buyer", "sarah" to "Seller")
		val signerUser = SignerUser.loadFromFile("adam", "command/adam")
		val signer = SsmCmdSignerSha256RSASigner(
			signerUser
		)

		val session = SsmSession(
			"Car dealership",
			"deal20181201",
			roles,
			"Used car for 100 dollars.",
			null
		)
		val (fcn, args) = StartCmd(session).invoke(chaincodeUri, signerUser.name, signer).buildArgs()
		args.forEach { s: String? -> println(s) }
		val expectedJson = "{\"ssm\":\"Car dealership\",\"session\":\"deal20181201\",\"roles\":" +
					"{\"chuck\":\"Buyer\",\"sarah\":\"Seller\"},\"public\":\"Used car for 100 dollars.\"}"
		Assertions.assertThat(fcn).isEqualTo("start")
		Assertions.assertThat(args)
			.isNotEmpty
			.containsExactly(
				expectedJson,
				"adam",
				rsaSignAsB64(expectedJson, signerUser.pair.private)
			)
	}
}
