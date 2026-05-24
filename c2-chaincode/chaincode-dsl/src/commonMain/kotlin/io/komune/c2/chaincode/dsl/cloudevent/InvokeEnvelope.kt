package io.komune.c2.chaincode.dsl.cloudevent

import f2.dsl.cqrs.envelope.EnvelopeDTO
import kotlinx.serialization.Serializable

/**
 * CloudEvents 1.0 structured-mode envelope used by `POST /invoke`.
 *
 * Extends F2's [EnvelopeDTO] with [subject] for request-response correlation:
 * responses carry the originating request `id` in `subject`.
 *
 * Producers MUST ensure `(source, id)` pairs are unique per CloudEvents 1.0 §3.1.1.
 */
@Serializable
class InvokeEnvelope<T>(
    override val id: String,
    override val data: T,
    override val type: String,
    override val datacontenttype: String? = "application/json",
    override val specversion: String? = CE_SPEC_VERSION,
    override val source: String? = null,
    override val time: String? = null,
    val subject: String? = null,
) : EnvelopeDTO<T> {
    companion object {
        const val CE_SPEC_VERSION = "1.0"
    }
}
