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
    @Test
    @Throws(Exception::class)
    fun testExecute() {
        val  chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
        val adamPair = loadKeyPair("command/adam")
        val signerUser = SignerUser("adam", adamPair)
        val signer = SsmCmdSignerSha256RSASigner(
            signerUser
        )
        val sell = SsmTransition(from = 0, to = 1, role = "Seller", action = "Sell")
        val buy = SsmTransition(from = 1, to = 2, role = "Buyer", action = "Buy")
        val ssm = Ssm("dealership", Lists.newArrayList(sell, buy))

        val (fcn, args) = CreateCmd(ssm).invoke(chaincodeUri, signerUser.name, signer).buildArgs()

        val expectedJson =
            """{"name":"dealership","transitions":[{"from":0,"to":1,"role":"Seller","action":"Sell"},{"from":1,"to":2,"role":"Buyer","action":"Buy"}]}"""
        Assertions.assertThat(fcn.value).isEqualTo("create")
        Assertions.assertThat(args)
            .isNotEmpty
            .containsExactly(
                expectedJson,
                "adam",
                rsaSignAsB64(expectedJson, signerUser.pair.private)
            )
    }
}
