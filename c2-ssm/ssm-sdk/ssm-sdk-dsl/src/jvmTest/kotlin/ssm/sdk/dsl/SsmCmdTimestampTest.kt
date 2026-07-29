package ssm.sdk.dsl

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Coverage for the optional business timestamp on [SsmCmd] and its passthrough via
 * [SsmCmdSigned.buildArgs] / [SsmCmdSigned.buildCommandArgs]. The timestamp is transport metadata —
 * it must reach the InvokeArgs/InvokeRequest but never the signed on-chain values.
 */
class SsmCmdTimestampTest {

    private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

    private fun signed(timestamp: Long?, performAction: String? = null) = SsmCmdSigned(
        cmd = SsmCmd(
            chaincodeUri = chaincodeUri,
            agentName = "admin",
            json = """{"k":"v"}""",
            command = SsmCmdName.START,
            performAction = performAction,
            valueToSign = "value-to-sign",
            timestamp = timestamp,
        ),
        signature = "sig",
        signer = "admin",
        chaincodeUri = chaincodeUri,
    )

    @Test
    fun `SsmCmd defaults timestamp to null`() {
        val cmd = SsmCmd(chaincodeUri, "admin", "{}", SsmCmdName.START, null, "v")
        assertThat(cmd.timestamp).isNull()
    }

    @Test
    fun `buildArgs carries the cmd timestamp and never leaks it into on-chain values`() {
        val args = signed(timestamp = 1_623_715_200_000L).buildArgs()
        assertThat(args.timestamp).isEqualTo(1_623_715_200_000L)
        assertThat(args.function.value).isEqualTo("start")
        assertThat(args.values).doesNotContain("1623715200000")
    }

    @Test
    fun `buildArgs keeps a null timestamp`() {
        assertThat(signed(timestamp = null).buildArgs().timestamp).isNull()
    }

    @Test
    fun `buildCommandArgs propagates the timestamp into the InvokeRequest`() {
        val request = signed(timestamp = 55L).buildCommandArgs(InvokeRequestType.invoke)
        assertThat(request.timestamp).isEqualTo(55L)
        assertThat(request.fcn).isEqualTo("start")
    }

    @Test
    fun `buildCommandArgs keeps a null timestamp`() {
        assertThat(signed(timestamp = null).buildCommandArgs(InvokeRequestType.invoke).timestamp).isNull()
    }
}
