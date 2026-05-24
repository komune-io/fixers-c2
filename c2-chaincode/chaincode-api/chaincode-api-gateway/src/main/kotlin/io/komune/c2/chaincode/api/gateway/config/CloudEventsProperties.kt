package io.komune.c2.chaincode.api.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * CloudEvents 1.0 producer config for `/invoke` responses.
 *
 * @property source the `source` attribute the gateway sets on every emitted CloudEvent.
 *                   Must be a URI-reference per CE spec §3.1.4.
 */
@ConfigurationProperties("c2.cloudevents")
data class CloudEventsProperties(
    val source: String = "/io.komune.c2/gateway",
)
