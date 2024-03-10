package ssm.data.spring.autoconfigure

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import ssm.bdd.spring.autoconfigure.steps.SpringBootAutoconfigureSteps

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = SpringBootAutoconfigureSteps.GLUE)
class DataSsmSpringCucumberTests
