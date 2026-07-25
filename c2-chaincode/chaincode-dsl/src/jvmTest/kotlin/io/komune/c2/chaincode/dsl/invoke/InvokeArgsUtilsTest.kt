package io.komune.c2.chaincode.dsl.invoke

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InvokeArgsUtilsTest {

    @Test
    fun `isListQuery matches the function name case-insensitively`() {
        assertThat(InvokeArgsUtils.isListQuery(InvokeArgs("list"))).isTrue()
        assertThat(InvokeArgsUtils.isListQuery(InvokeArgs("LIST"))).isTrue()
        assertThat(InvokeArgsUtils.isListQuery(InvokeArgs("query"))).isFalse()
    }

    @Test
    fun `isQueryFunction matches the query function case-insensitively`() {
        assertThat(InvokeArgsUtils.isQueryFunction(InvokeArgs("query"))).isTrue()
        assertThat(InvokeArgsUtils.isQueryFunction(InvokeArgs("QUERY"))).isTrue()
        assertThat(InvokeArgsUtils.isQueryFunction(InvokeArgs("list"))).isFalse()
    }

    @Test
    fun `isBlockQuery matches on the function name when values are present`() {
        assertThat(InvokeArgsUtils.isBlockQuery(InvokeArgs("block", "42"))).isTrue()
        assertThat(InvokeArgsUtils.isBlockQuery(InvokeArgs("BLOCK", "42"))).isTrue()
    }

    @Test
    fun `isBlockQuery matches on the first value when the function differs`() {
        assertThat(InvokeArgsUtils.isBlockQuery(InvokeArgs("query", "block"))).isTrue()
        assertThat(InvokeArgsUtils.isBlockQuery(InvokeArgs("query", "other"))).isFalse()
    }

    @Test
    fun `isBlockQuery is false without values even for a block function`() {
        assertThat(InvokeArgsUtils.isBlockQuery(InvokeArgs("block"))).isFalse()
    }

    @Test
    fun `isTransactionQuery follows the same function-or-first-value rule`() {
        assertThat(InvokeArgsUtils.isTransactionQuery(InvokeArgs("transaction", "tx1"))).isTrue()
        assertThat(InvokeArgsUtils.isTransactionQuery(InvokeArgs("query", "transaction"))).isTrue()
        assertThat(InvokeArgsUtils.isTransactionQuery(InvokeArgs("transaction"))).isFalse()
        assertThat(InvokeArgsUtils.isTransactionQuery(InvokeArgs("query", "list"))).isFalse()
    }
}
