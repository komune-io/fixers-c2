package io.komune.c2.chaincode.dsl.cloudevent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InvokeEnvelopeTest {

    @Test
    fun `defaults follow the cloudevents structured mode contract`() {
        val envelope = InvokeEnvelope(
            id = "client-uuid-1",
            data = "payload",
            type = "io.komune.c2.invoke",
        )
        assertThat(envelope.id).isEqualTo("client-uuid-1")
        assertThat(envelope.data).isEqualTo("payload")
        assertThat(envelope.type).isEqualTo("io.komune.c2.invoke")
        assertThat(envelope.datacontenttype).isEqualTo("application/json")
        assertThat(envelope.specversion).isEqualTo(InvokeEnvelope.CE_SPEC_VERSION)
        assertThat(envelope.source).isNull()
        assertThat(envelope.time).isNull()
        assertThat(envelope.subject).isNull()
    }

    @Test
    fun `subject carries the originating request id for responses`() {
        val envelope = InvokeEnvelope(
            id = "response-1",
            data = mapOf("status" to "Committed"),
            type = "io.komune.c2.invoke.outcome.committed",
            source = "/io.komune.c2/gateway",
            time = "2026-05-22T10:30:00Z",
            subject = "client-uuid-1",
        )
        assertThat(envelope.subject).isEqualTo("client-uuid-1")
        assertThat(envelope.source).isEqualTo("/io.komune.c2/gateway")
        assertThat(envelope.time).isEqualTo("2026-05-22T10:30:00Z")
    }
}
