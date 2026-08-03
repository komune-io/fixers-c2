package ssm.chaincode.dsl.query

import io.komune.c2.chaincode.dsl.ChaincodeUri
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmSessionState

class SsmQueryDslTest {

    private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

    @Test
    fun `get queries carry the chaincode uri and their target identifier`() {
        assertThat(SsmGetQuery(chaincodeUri, "CarDealership").name).isEqualTo("CarDealership")
        assertThat(SsmGetQuery(chaincodeUri, "CarDealership").chaincodeUri).isEqualTo(chaincodeUri)
        assertThat(SsmGetAdminQuery(chaincodeUri, "Chuck").name).isEqualTo("Chuck")
        assertThat(SsmGetUserQuery(chaincodeUri, "Sarah").name).isEqualTo("Sarah")
        assertThat(SsmGetSessionQuery(chaincodeUri, "deal20181201").sessionName).isEqualTo("deal20181201")
        assertThat(SsmGetTransactionQuery(chaincodeUri, "tx-1").id).isEqualTo("tx-1")
    }

    @Test
    fun `get session logs query targets a session of a ssm`() {
        val query = SsmGetSessionLogsQuery(
            chaincodeUri = chaincodeUri,
            ssmName = "CarDealership",
            sessionName = "deal20181201",
        )
        assertThat(query.ssmName).isEqualTo("CarDealership")
        assertThat(query.sessionName).isEqualTo("deal20181201")
        assertThat(query.chaincodeUri).isEqualTo(chaincodeUri)
    }

    @Test
    fun `get session logs result groups logs per session`() {
        val result = SsmGetSessionLogsQueryResult(
            ssmName = "CarDealership",
            sessionName = "deal20181201",
            logs = emptyList(),
        )
        assertThat(result.ssmName).isEqualTo("CarDealership")
        assertThat(result.sessionName).isEqualTo("deal20181201")
        assertThat(result.logs).isEmpty()
    }

    @Test
    fun `list queries only need the chaincode uri`() {
        assertThat(SsmListAdminQuery(chaincodeUri).chaincodeUri).isEqualTo(chaincodeUri)
        assertThat(SsmListUserQuery(chaincodeUri).chaincodeUri).isEqualTo(chaincodeUri)
        assertThat(SsmListSsmQuery(chaincodeUri).chaincodeUri).isEqualTo(chaincodeUri)
        assertThat(SsmListSessionQuery(chaincodeUri).chaincodeUri).isEqualTo(chaincodeUri)
    }

    @Test
    fun `item results expose the found item or null`() {
        val ssm = Ssm("CarDealership", emptyList())
        assertThat(SsmGetResult(ssm).item).isEqualTo(ssm)
        assertThat(SsmGetResult(null).item).isNull()
        val agent = Agent("Chuck", byteArrayOf(1))
        assertThat(SsmGetAdminResult(agent).item).isEqualTo(agent)
        assertThat(SsmGetUserResult(agent).item).isEqualTo(agent)
        val state = SsmSessionState(null, "deal", null, null, origin = null, current = 0, iteration = 0)
        assertThat(SsmGetSessionResult(state).item).isEqualTo(state)
    }

    @Test
    fun `items results expose the listed names`() {
        assertThat(SsmListAdminResult(arrayOf("Chuck")).items).containsExactly("Chuck")
        assertThat(SsmListUserResult(arrayOf("Sarah")).items).containsExactly("Sarah")
        assertThat(SsmListSsmResult(arrayOf("CarDealership")).items).containsExactly("CarDealership")
        assertThat(SsmListSessionResult(arrayOf("deal20181201")).items).containsExactly("deal20181201")
    }
}
