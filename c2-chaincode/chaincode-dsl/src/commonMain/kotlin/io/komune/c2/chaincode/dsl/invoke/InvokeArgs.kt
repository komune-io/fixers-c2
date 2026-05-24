package io.komune.c2.chaincode.dsl.invoke

data class InvokeArgs(
	val function: String,
	val values: List<String>,
)
