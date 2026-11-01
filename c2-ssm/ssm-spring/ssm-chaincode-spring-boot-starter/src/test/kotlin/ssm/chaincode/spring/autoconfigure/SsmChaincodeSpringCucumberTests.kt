package ssm.chaincode.spring.autoconfigure

import io.cucumber.junit.platform.engine.Constants.GLUE_HINT_ENABLED_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import ssm.bdd.spring.autoconfigure.steps.SpringBootAutoconfigureSteps

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = SpringBootAutoconfigureSteps.GLUE)
// Cucumber 8 glue hints call Class.getDeclaringClass() on every glue class, which throws
// IncompatibleClassChangeError on Kotlin coroutine-inlined classes.
@ConfigurationParameter(key = GLUE_HINT_ENABLED_PROPERTY_NAME, value = "false")
class SsmChaincodeSpringCucumberTests
