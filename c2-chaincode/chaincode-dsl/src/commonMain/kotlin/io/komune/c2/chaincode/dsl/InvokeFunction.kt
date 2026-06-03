package io.komune.c2.chaincode.dsl

import kotlin.jvm.JvmInline

@JvmInline
value class InvokeFunction(val value: String) {
	override fun toString(): String = value
}
