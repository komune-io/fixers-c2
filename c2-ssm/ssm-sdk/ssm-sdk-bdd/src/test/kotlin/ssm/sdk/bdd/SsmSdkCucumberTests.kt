package ssm.sdk.bdd

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import ssm.sdk.bdd.query.SdkQuerySteps
import ssm.sdk.bdd.tx.SdkTxSteps

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "${SdkQuerySteps.GLUE}, ${SdkTxSteps.GLUE}")
// Cucumber 8 glue hints call Class.getDeclaringClass() on every glue class, which throws
// IncompatibleClassChangeError on Kotlin coroutine-inlined classes.
@ConfigurationParameter(key = Constants.GLUE_HINT_ENABLED_PROPERTY_NAME, value = "false")
class SsmSdkCucumberTests
