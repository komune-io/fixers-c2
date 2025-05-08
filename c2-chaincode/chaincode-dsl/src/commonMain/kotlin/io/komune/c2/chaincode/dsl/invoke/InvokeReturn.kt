package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.TransactionId


data class InvokeReturn(
    val status: String,
    val info: String,
    val transactionId: TransactionId,
)
