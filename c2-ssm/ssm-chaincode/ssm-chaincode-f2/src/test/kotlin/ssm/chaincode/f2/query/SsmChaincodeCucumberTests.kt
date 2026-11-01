package ssm.chaincode.f2.query

import io.cucumber.junit.platform.engine.Constants.GLUE_HINT_ENABLED_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import ssm.chaincode.bdd.SsmChaincodeBddSteps
import ssm.sdk.bdd.tx.SdkTxSteps

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "${SsmChaincodeBddSteps.GLUE},${SdkTxSteps.GLUE}")
// Cucumber 8 glue hints call Class.getDeclaringClass() on every glue class, which throws
// IncompatibleClassChangeError on Kotlin coroutine-inlined classes.
@ConfigurationParameter(key = GLUE_HINT_ENABLED_PROPERTY_NAME, value = "false")
class SsmChaincodeCucumberTests
