package ssm.api.features.query

import f2.dsl.fnc.invoke
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ssm.api.DataSsmQueryFunctionImpl
import ssm.bdd.config.SsmBddConfig
import ssm.data.dsl.features.query.DataSsmListQuery

class DataSsmListQueryFunctionImpTest {
	private val dataSsmQueryFunction = DataSsmQueryFunctionImpl(
		SsmBddConfig.Data.config,
	)

	private val function = dataSsmQueryFunction.dataSsmListQueryFunction()

	@Disabled
	@Test
	suspend fun `must return result`() {
		val response = function.invoke(DataSsmListQuery(emptyList()))
		Assertions.assertThat(response.items).hasSize(2)
	}

}
