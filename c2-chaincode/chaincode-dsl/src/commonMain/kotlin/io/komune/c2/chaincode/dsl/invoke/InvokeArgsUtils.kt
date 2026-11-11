package io.komune.c2.chaincode.dsl.invoke

object InvokeArgsUtils {
    var BLOCK_QUERY: String = "block"
    var LIST_QUERY: String = "list"
    var TRANSACTION_QUERY: String = "transaction"
    var QUERY_FUNCTION: String = "query"

    fun isBlockQuery(args: InvokeArgs): Boolean = isQueryOfType(args, BLOCK_QUERY)

    fun isListQuery(args: InvokeArgs): Boolean = args.function.value.equals(LIST_QUERY, ignoreCase = true)

    fun isTransactionQuery(args: InvokeArgs): Boolean = isQueryOfType(args, TRANSACTION_QUERY)

    private fun isQueryOfType(args: InvokeArgs, type: String): Boolean =
        args.values.isNotEmpty() &&
            (args.function.value.equals(type, ignoreCase = true) || args.values[0].equals(type, ignoreCase = true))

    fun isQueryFunction(args: InvokeArgs): Boolean = args.function.value.equals(QUERY_FUNCTION, ignoreCase = true)
}
