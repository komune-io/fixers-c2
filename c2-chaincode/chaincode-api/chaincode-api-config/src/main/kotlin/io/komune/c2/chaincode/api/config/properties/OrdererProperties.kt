package io.komune.c2.chaincode.api.config.properties

import io.komune.c2.chaincode.api.config.utils.HasTlsCacerts

class OrdererProperties(
    val url: String,
    val serverHostname: String,
    override val tlsCacerts: String
) : HasTlsCacerts
