package ssm.sdk.core.invoke.builder

import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import ssm.sdk.core.invoke.query.SsmQueryName

interface HasQueryName {
	val queryName: SsmQueryName
}

interface HasGet: HasQueryName {
	fun queryArgs(username: String): InvokeArgs {
		return InvokeArgs(queryName.value, listOf(username))
	}

}

interface HasList: HasQueryName {

	companion object {
		const val LIST_FUNCTION = "list"
	}

	fun listArgs(): InvokeArgs {
		return InvokeArgs(
			function = LIST_FUNCTION,
			values = listOf(queryName.value)
		)
	}
}

open class QueryBuilder(override val queryName: SsmQueryName): HasQueryName
