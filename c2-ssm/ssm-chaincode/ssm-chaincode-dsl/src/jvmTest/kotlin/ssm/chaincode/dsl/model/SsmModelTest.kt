package ssm.chaincode.dsl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.config.SsmChaincodeProperties

class SsmModelTest {

    private val transition = SsmTransition(from = 0, to = 1, role = "Seller", action = "Sell")

    @Test
    fun `a chaincode exposes its id and channel`() {
        val chaincode = Chaincode(id = "ssm", channelId = "sandbox")
        assertThat(chaincode.id).isEqualTo("ssm")
        assertThat(chaincode.channelId).isEqualTo("sandbox")
        assertThat(chaincode).isEqualTo(Chaincode("ssm", "sandbox"))
    }

    @Test
    fun `a ssm is defined by its name and transitions`() {
        val ssm = Ssm(name = "CarDealership", transitions = listOf(transition))
        assertThat(ssm.name).isEqualTo("CarDealership")
        assertThat(ssm.transitions).containsExactly(transition)
        assertThat(ssm).isEqualTo(Ssm("CarDealership", listOf(transition)))
    }

    @Test
    fun `a transition links states with a role and an action`() {
        assertThat(transition.from).isEqualTo(0)
        assertThat(transition.to).isEqualTo(1)
        assertThat(transition.role).isEqualTo("Seller")
        assertThat(transition.action).isEqualTo("Sell")
        assertThat(transition).isNotEqualTo(transition.copy(to = 2))
    }

    @Test
    fun `a context carries session data for an iteration`() {
        val context = SsmContext(
            session = "deal20181201",
            public = "Used car for 100 dollars.",
            iteration = 2,
        )
        assertThat(context.session).isEqualTo("deal20181201")
        assertThat(context.public).isEqualTo("Used car for 100 dollars.")
        assertThat(context.iteration).isEqualTo(2)
        assertThat(context.private).isNull()
    }

    @Test
    fun `a grant associates credits to a user for an iteration`() {
        val grant = SsmGrant(
            user = "chuck",
            iteration = 1,
            credits = mapOf("read" to Credit(amount = 3)),
        )
        assertThat(grant.user).isEqualTo("chuck")
        assertThat(grant.iteration).isEqualTo(1)
        assertThat(grant.credits.getValue("read").amount).isEqualTo(3)
    }

    @Test
    fun `a session instantiates a ssm with roles and public data`() {
        val session = SsmSession(
            ssm = "CarDealership",
            session = "deal20181201",
            roles = mapOf("chuck" to "Buyer"),
            public = "Used car for 100 dollars.",
        )
        assertThat(session.ssm).isEqualTo("CarDealership")
        assertThat(session.session).isEqualTo("deal20181201")
        assertThat(session.roles).containsEntry("chuck", "Buyer")
        assertThat(session.public).isEqualTo("Used car for 100 dollars.")
        assertThat(session.private).isEmpty()
    }

    @Test
    fun `a session state snapshots the session at an iteration`() {
        val state = SsmSessionState(
            ssm = "CarDealership",
            session = "deal20181201",
            roles = mapOf("chuck" to "Buyer"),
            public = "Used car for 100 dollars.",
            origin = transition,
            current = 1,
            iteration = 3,
        )
        assertThat(state.ssm).isEqualTo("CarDealership")
        assertThat(state.session).isEqualTo("deal20181201")
        assertThat(state.origin).isEqualTo(transition)
        assertThat(state.current).isEqualTo(1)
        assertThat(state.iteration).isEqualTo(3)
        assertThat(state.private).isEmpty()
    }

    @Test
    fun `a session state log ties a state to its transaction`() {
        val state = SsmSessionState(
            ssm = null,
            session = "deal20181201",
            roles = null,
            public = null,
            origin = null,
            current = 0,
            iteration = 0,
        )
        val log = SsmSessionStateLog(txId = "tx-1", state = state)
        assertThat(log.txId).isEqualTo("tx-1")
        assertThat(log.state).isEqualTo(state)
    }

    @Test
    fun `chaincode properties expose the peer url`() {
        val properties = SsmChaincodeProperties(url = "http://peer.sandbox.komune.io:9000")
        assertThat(properties.url).isEqualTo("http://peer.sandbox.komune.io:9000")
    }
}
