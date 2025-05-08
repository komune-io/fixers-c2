package io.komune.c2.chaincode.dsl.invoke


object InvokeArgsUtils {
    var BLOCK_QUERY: String = "block"
    var LIST_QUERY: String = "list"
    var TRANSACTION_QUERY: String = "transaction"
    var QUERY_FUNCTION: String = "query"

    fun isBlockQuery(args: InvokeArgs): Boolean {
        return isQueryOfType(args, BLOCK_QUERY)
    }

    fun isListQuery(args: InvokeArgs): Boolean {
        return args.function.equals(LIST_QUERY, ignoreCase = true)
    }

    fun isTransactionQuery(args: InvokeArgs): Boolean {
        return isQueryOfType(args, TRANSACTION_QUERY)
    }

    private fun isQueryOfType(args: InvokeArgs, type: String): Boolean {
        return args.values.isNotEmpty() && (args.function.equals(type, ignoreCase = true) || args.values
            .get(0).equals(type, ignoreCase = true)
                )
    }

    fun isQueryFunction(args: InvokeArgs): Boolean {
        return args.function.equals(QUERY_FUNCTION, ignoreCase = true)
    }
}
