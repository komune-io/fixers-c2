package io.komune.c2.chaincode.api.gateway.chaincode.model

import io.komune.c2.chaincode.dsl.invoke.InvokeRequest

data class InvokeRequestV2(
    val msgId: String,
    val request: InvokeRequest,
)
