package ssm.api.features.query

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.api.DataSsmQueryFunctionImpl
import ssm.bdd.config.SsmBddConfig
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.data.dsl.features.query.DataSsmSessionGetQuery
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction

internal class DataSsmSessionGetQueryFunctionImplTest {

	private val dataSsmQueryFunction = DataSsmQueryFunctionImpl(
		SsmBddConfig.Data.config,
	)

	private val dataSsmSessionGetQueryFunction: DataSsmSessionGetQueryFunction
		= dataSsmQueryFunction.dataSsmSessionGetQueryFunction()


	@Test
	suspend fun `test exception`() {
		val queries = flowOf(
			DataSsmSessionGetQuery(
				"sessionName",
				SsmBddConfig.Chaincode.chaincodeUri.toSsmUri("ssmName")
			)
		)

		val ssmListResult = dataSsmSessionGetQueryFunction.invoke(queries).toList()
		Assertions.assertThat(ssmListResult).hasSize(1)
		ssmListResult.map {
			Assertions.assertThat(it.item).isNull()
		}

	}
}
