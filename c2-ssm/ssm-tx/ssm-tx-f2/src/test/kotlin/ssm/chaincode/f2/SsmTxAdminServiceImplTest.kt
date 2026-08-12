package ssm.chaincode.f2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxInitFunctionImpl
import ssm.chaincode.f2.features.command.SsmUserGrantFunctionImpl
import ssm.chaincode.f2.features.command.SsmUserRegisterFunctionImpl

internal class SsmTxAdminServiceImplTest {

	private val repository = StubSsmChaincodeRepository()

	private val service = SsmTxAdminServiceImpl(
		ssmTxService = SsmTxTestFixtures.txService(repository),
		ssmQueryService = SsmTxTestFixtures.queryService(repository),
	)

	@Test
	fun `exposes each admin function backed by its implementation`() {
		assertThat(service.ssmTxUserGrantFunction()).isInstanceOf(SsmUserGrantFunctionImpl::class.java)
		assertThat(service.ssmTxUserRegisterFunction()).isInstanceOf(SsmUserRegisterFunctionImpl::class.java)
		assertThat(service.ssmTxCreateFunction()).isInstanceOf(SsmTxCreateFunctionImpl::class.java)
		assertThat(service.ssmTxInitializeFunction()).isInstanceOf(SsmTxInitFunctionImpl::class.java)
	}
}
