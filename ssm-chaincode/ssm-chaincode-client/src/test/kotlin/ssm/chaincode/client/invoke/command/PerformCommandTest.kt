package ssm.chaincode.client.invoke.command

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.client.loadFromFile
import ssm.chaincode.dsl.SsmContext
import ssm.sdk.sign.crypto.KeyPairReader.Companion.loadKeyPair
import ssm.sdk.sign.model.Signer
import java.util.function.Consumer

class PerformCommandTest {
    @Test
    @Throws(Exception::class)
    fun test_execute() {
        val adamPair = loadKeyPair("command/adam")
        val signer = Signer("adam", adamPair)

//        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}"
        val context = SsmContext("deal20181201", "100 dollars 1978 Camaro", 0, null)
        val (fcn, args) = PerformCommandSigner(signer, "Sell", context).invoke()
        args.forEach(Consumer { s: String? -> println(s) })
        Assertions.assertThat(fcn).isEqualTo("perform")
        Assertions.assertThat(args)
            .isNotEmpty
            .containsExactly(
                "Sell",
                "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}",
                "adam",
                "GOqqfALK7M682IFEBU66GG8aIevDc5mGa3W/o7UXhsvweliz/1XWxWdNOfcEvMJ9DnfhrX1Bupyo956EliiFmutcbi0Hvp+IuzgjbcKUtY2jAGKWA8QLmBeN2DGnYMgor4aJiOZKS+NnA3coNz/usHalIAzqZlihq41ZANpr1Uqjc728CNXUOVuIeRJ0oWRo8X18SO0o+VRB+/0+BzGexu6In/zcoaEjDlMBtB29U0hFgB1DX28Ek0snuZYMsacL76MpKXZjr8v2Sc1oQ63PSUSrjSmCzni0lZ28AT4VJoui2LU2FKLsCqKLKyi+pIP82a+9ahdmwIQ5r0X4dpGCIQ=="
            )
    }

    @Test
    @Throws(Exception::class)
    fun test_executeWithPrivateMessage() {
        val adamPair = loadKeyPair("command/adam")
        val signer = Signer("adam", adamPair)
        val (name, pub) = loadFromFile("vivi", "command/adam")

//        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}"
        val context = SsmContext("deal20181201", "100 dollars 1978 Camaro", 0, mapOf("vivi" to "message"))
        val (fcn, args) = PerformCommandSigner(signer, "Sell", context).invoke()
        args.forEach(Consumer { s: String? -> println(s) })
        Assertions.assertThat(fcn).isEqualTo("perform")
        Assertions.assertThat(args)
            .isNotEmpty
            .containsExactly(
                "Sell",
                "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0,\"private\":{\"vivi\":\"message\"}}",
                "adam",
                "mBWkUXlR+4WKYDU6ZWur7bRjnal2jfccb9GQpvNwOiYxQ0XuRiqExHOyBNt1zyGU0qRzkEuGWAEGhsnHBQEjLeVlF+Dkf92llp/NwBymN3N/hPd6ha6M0OPkSUeu+kqbUuhVw7GqvoqBo0A5qkbeKd8ue/lbfi9bhJXTxOLzIEanjZqNnfTcmkkSZFtv3FsDQ4fbt6BZfOGpoLn3HcJZP1lTPROlLbuxzD+YdF7pBEQDL3S8NVIHGHwK7RAaxPrazABy1VdJUlfnScpk8VjiVxUlAeGu985s/6N7Zv8sF/0c7r4b0Pjg7koiasMFuafNOWVpTc2tTp/4AqHuS4fKAw=="
            )
    }
}