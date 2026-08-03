package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.invoke.InvokeException.Companion.asInvokeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * InvokeRequest hand-writes equals/hashCode because `args` is an Array (reference
 * equality by default) — these tests lock in content-based comparison.
 */
class InvokeRequestTest {

    private fun request(args: Array<String> = arrayOf("a", "b")) = InvokeRequest(
        channelid = "sandbox",
        chaincodeid = "ssm",
        cmd = InvokeRequestType.query,
        fcn = "session",
        args = args,
        timestamp = 42L,
    )

    @Test
    fun `requests with equal args content are equal even across distinct arrays`() {
        val a = request(arrayOf("a", "b"))
        val b = request(arrayOf("a", "b"))
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `requests differing by any field are not equal`() {
        val a = request()
        assertThat(a).isNotEqualTo(request(arrayOf("a", "c")))
        assertThat(a).isNotEqualTo(a.copy(channelid = "other"))
        assertThat(a).isNotEqualTo(a.copy(chaincodeid = "other"))
        assertThat(a).isNotEqualTo(a.copy(cmd = InvokeRequestType.invoke))
        assertThat(a).isNotEqualTo(a.copy(fcn = "other"))
        assertThat(a).isNotEqualTo(a.copy(timestamp = 43L))
    }

    @Test
    fun `a request equals itself and never equals null or another type`() {
        val a = request()
        assertThat(a).isEqualTo(a)
        assertThat(a).isNotEqualTo(null)
        assertThat(a).isNotEqualTo("request")
    }

    @Test
    fun `toInvokeArgs maps function args and timestamp`() {
        val invokeArgs = request().toInvokeArgs()
        assertThat(invokeArgs.function).isEqualTo(InvokeFunction("session"))
        assertThat(invokeArgs.values).containsExactly("a", "b")
        assertThat(invokeArgs.timestamp).isEqualTo(42L)
    }

    @Test
    fun `toInvokeArgs maps each request of a list`() {
        val requests = listOf(request(), request(arrayOf("c")))
        val all = requests.toInvokeArgs()
        assertThat(all).hasSize(2)
        assertThat(all[1].values).containsExactly("c")
    }

    @Test
    fun `invoke function renders its raw value`() {
        assertThat(InvokeFunction("session").toString()).isEqualTo("session")
    }

    @Test
    fun `asInvokeException joins messages`() {
        val exception = listOf("boom", "bam").asInvokeException()
        assertThat(exception.message).isEqualTo("boom;bam")
        assertThat(exception.cause).isNull()
    }
}
