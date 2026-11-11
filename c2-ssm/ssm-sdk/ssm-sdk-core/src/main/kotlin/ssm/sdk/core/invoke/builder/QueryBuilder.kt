package ssm.sdk.core.invoke.builder

import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import ssm.sdk.core.invoke.query.SsmQueryName

interface HasQueryName {
    val queryName: SsmQueryName
}

interface HasGet: HasQueryName {
    fun queryArgs(username: String): InvokeArgs = InvokeArgs(InvokeFunction(queryName.value), listOf(username))

}

interface HasList: HasQueryName {

    fun listArgs(): InvokeArgs {
        return InvokeArgs(
            function = InvokeFunction(LIST_FUNCTION),
            values = listOf(queryName.value)
        )
    }

    companion object {
        const val LIST_FUNCTION = "list"
    }
}

open class QueryBuilder(override val queryName: SsmQueryName): HasQueryName
