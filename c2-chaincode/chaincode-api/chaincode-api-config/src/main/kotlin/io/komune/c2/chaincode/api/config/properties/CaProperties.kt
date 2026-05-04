package io.komune.c2.chaincode.api.config.properties

import io.komune.c2.chaincode.api.config.utils.HasTlsCacerts


data class CaProperties(
    val name: String,
    val url: String,
    override val tlsCacerts: String
) : HasTlsCacerts
