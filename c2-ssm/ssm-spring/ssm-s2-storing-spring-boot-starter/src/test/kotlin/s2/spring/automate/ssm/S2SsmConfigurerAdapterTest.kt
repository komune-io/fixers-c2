package s2.spring.automate.ssm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.spring.automate.executor.S2AutomateExecutorSpring
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri

/**
 * Coverage for the default [S2SsmConfigurerAdapter.businessTimestampOf] hook, which returns null unless a
 * concrete configurer overrides it (as the ssm-delivery gateway does).
 */
class S2SsmConfigurerAdapterTest {

    interface TestState : S2State { override val position: Int }

    data class TestEntity(val id: String, val state: Int) : WithS2Id<String>, WithS2State<TestState> {
        override fun s2Id() = id
        override fun s2State() = object : TestState { override val position = state }
    }

    class TestExecutor : S2AutomateExecutorSpring<TestState, String, TestEntity>()

    private class TestAdapter : S2SsmConfigurerAdapter<TestState, String, TestEntity, TestExecutor>() {
        override fun entityType() = TestEntity::class.java
        override fun chaincodeUri() = ChaincodeUri("chaincode:sandbox:ssm")
        override fun signerAgent() = Agent(name = "agent", pub = ByteArray(0))
        override fun executor() = TestExecutor()
        override fun automate() = S2Automate("test-ssm", null, arrayOf())
    }

    @Test
    fun `businessTimestampOf defaults to null`() {
        assertThat(TestAdapter().businessTimestampOf("any-message")).isNull()
    }
}
