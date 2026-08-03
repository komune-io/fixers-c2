package ssm.api.features.query

import f2.dsl.fnc.invoke
import kotlinx.coroutines.flow.map
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.query.SsmGetQueryFunction
import ssm.chaincode.dsl.query.SsmGetResult
import ssm.data.dsl.features.query.DataSsmGetQuery

class DataSsmGetQueryFunctionImplUnitTest {

	private val ssmUri = SsmUri("ssm:sandbox:ssm:CarDealership")

	@Test
	suspend fun `returns the data ssm built from the chaincode ssm`() {
		val ssm = Ssm("CarDealership", emptyList())
		val getFunction = SsmGetQueryFunction { msgs ->
			msgs.map { query ->
				assertThat(query.chaincodeUri).isEqualTo(ssmUri.chaincodeUri)
				assertThat(query.name).isEqualTo("CarDealership")
				SsmGetResult(ssm)
			}
		}
		val function = DataSsmGetQueryFunctionImpl(getFunction)

		val result = function.invoke(DataSsmGetQuery(ssmUri))

		val item = result.item
		assertThat(item).isNotNull
		assertThat(item?.ssm).isEqualTo(ssm)
		assertThat(item?.uri).isEqualTo(ssmUri)
		assertThat(item?.channel?.id).isEqualTo("sandbox")
	}

	@Test
	suspend fun `returns a null item when the ssm is unknown`() {
		val getFunction = SsmGetQueryFunction { msgs ->
			msgs.map { SsmGetResult(null) }
		}
		val function = DataSsmGetQueryFunctionImpl(getFunction)

		val result = function.invoke(DataSsmGetQuery(ssmUri))

		assertThat(result.item).isNull()
	}
}
