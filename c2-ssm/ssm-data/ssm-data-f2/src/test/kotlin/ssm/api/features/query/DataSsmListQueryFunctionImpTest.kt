package ssm.api.features.query

import f2.dsl.fnc.invoke
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ssm.api.DataSsmQueryFunctionImpl
import ssm.bdd.config.SsmBddConfig
import ssm.chaincode.dsl.config.groupBy
import ssm.data.dsl.features.query.DataSsmListQuery

class DataSsmListQueryFunctionImpTest {
	private val dataSsmQueryFunction = DataSsmQueryFunctionImpl(
		SsmBddConfig.Data.config,
	)

	private val function = dataSsmQueryFunction.dataSsmListQueryFunction()

	@Disabled
	@Test
	fun `must return result`() = runBlocking<Unit> {
		val response = function.invoke(DataSsmListQuery(emptyList()))
		Assertions.assertThat(response.items).hasSize(2)
	}

}
