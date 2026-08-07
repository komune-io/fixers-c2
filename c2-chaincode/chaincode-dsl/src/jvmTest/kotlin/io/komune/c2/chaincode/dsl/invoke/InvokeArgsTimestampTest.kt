package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.InvokeFunction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Coverage for the optional business timestamp added to [InvokeArgs] / [InvokeRequest]:
 * both constructors, the default null, and the [toInvokeArgs] passthrough.
 */
class InvokeArgsTimestampTest {

    @Test
    fun `primary constructor defaults timestamp to null and carries it when set`() {
        assertThat(InvokeArgs(InvokeFunction("fn"), listOf("a")).timestamp).isNull()
        assertThat(InvokeArgs(InvokeFunction("fn"), listOf("a"), timestamp = 1_623_715_200_000L).timestamp)
            .isEqualTo(1_623_715_200_000L)
    }

    @Test
    fun `string-vararg constructor keeps values and defaults timestamp to null`() {
        val args = InvokeArgs("fn", "a", "b")
        assertThat(args.function.value).isEqualTo("fn")
        assertThat(args.values).containsExactly("a", "b")
        assertThat(args.timestamp).isNull()
    }

    @Test
    fun `string-vararg constructor accepts a named timestamp`() {
        val args = InvokeArgs("fn", "a", timestamp = 42L)
        assertThat(args.values).containsExactly("a")
        assertThat(args.timestamp).isEqualTo(42L)
    }

    @Test
    fun `InvokeRequest toInvokeArgs propagates fcn, args and timestamp`() {
        val request = InvokeRequest(
            channelid = "ch",
            chaincodeid = "cc",
            cmd = InvokeRequestType.invoke,
            fcn = "fn",
            args = arrayOf("a", "b"),
            timestamp = 99L,
        )
        val args = request.toInvokeArgs()
        assertThat(args.function.value).isEqualTo("fn")
        assertThat(args.values).containsExactly("a", "b")
        assertThat(args.timestamp).isEqualTo(99L)
    }

    @Test
    fun `InvokeRequest defaults timestamp to null and toInvokeArgs preserves that`() {
        val request = InvokeRequest(cmd = InvokeRequestType.invoke, fcn = "fn", args = arrayOf("x"))
        assertThat(request.timestamp).isNull()
        assertThat(request.toInvokeArgs().timestamp).isNull()
    }

    @Test
    fun `list toInvokeArgs maps each element preserving timestamps`() {
        val requests = listOf(
            InvokeRequest(cmd = InvokeRequestType.invoke, fcn = "f1", args = arrayOf("a"), timestamp = 1L),
            InvokeRequest(cmd = InvokeRequestType.invoke, fcn = "f2", args = arrayOf("b")),
        )
        val mapped = requests.toInvokeArgs()
        assertThat(mapped.map { it.timestamp }).containsExactly(1L, null)
    }
}
