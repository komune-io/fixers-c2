package ssm.sync.sdk.cucumber

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import ssm.tx.bdd.TxSteps

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/sync")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "${CouchdbSsmSteps.GLUE},${TxSteps.GLUE}")
class SyncCucumberTest
