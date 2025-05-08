package io.komune.c2.chaincode.dsl.invoke

class InvokeException(
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    companion object {
        fun List<String>.asInvokeException(): InvokeException {
            return InvokeException(this.joinToString(";"))
        }
    }
}

