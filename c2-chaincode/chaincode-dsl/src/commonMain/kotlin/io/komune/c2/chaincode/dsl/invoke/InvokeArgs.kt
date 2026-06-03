package io.komune.c2.chaincode.dsl.invoke

import io.komune.c2.chaincode.dsl.InvokeFunction

data class InvokeArgs(
	val function: InvokeFunction,
	val values: List<String>,
) {
	constructor(function: String, vararg values: String) :
		this(InvokeFunction(function), values.toList())
}
