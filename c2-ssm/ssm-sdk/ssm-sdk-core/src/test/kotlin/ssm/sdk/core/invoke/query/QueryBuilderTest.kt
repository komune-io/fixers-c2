package ssm.sdk.core.invoke.query

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.sdk.core.invoke.builder.HasList

class QueryBuilderTest {

    @Test
    fun `block query builds get and list invoke args`() {
        val query = BlockQuery()
        assertThat(query.queryName).isEqualTo(SsmQueryName.BLOCK)

        val getArgs = query.queryArgs("10")
        assertThat(getArgs.function.value).isEqualTo("block")
        assertThat(getArgs.values).containsExactly("10")

        val listArgs = query.listArgs()
        assertThat(listArgs.function.value).isEqualTo(HasList.LIST_FUNCTION)
        assertThat(listArgs.values).containsExactly("block")
    }

    @Test
    fun `transaction query builds get and list invoke args`() {
        val query = TransactionQuery()
        assertThat(query.queryName).isEqualTo(SsmQueryName.TRANSACTION)
        assertThat(query.queryArgs("tx-1").function.value).isEqualTo("transaction")
        assertThat(query.queryArgs("tx-1").values).containsExactly("tx-1")
        assertThat(query.listArgs().values).containsExactly("transaction")
    }

    @Test
    fun `log query only supports get invoke args`() {
        val query = LogQuery()
        assertThat(query.queryName).isEqualTo(SsmQueryName.LOG)
        val args = query.queryArgs("deal20181201")
        assertThat(args.function.value).isEqualTo("log")
        assertThat(args.values).containsExactly("deal20181201")
    }
}
