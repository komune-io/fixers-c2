package ssm.couchdb.bdd.query

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite
import ssm.couchdb.bdd.CouchdbSsmSteps
import ssm.tx.bdd.TxSteps

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "${CouchdbSsmSteps.GLUE},${TxSteps.GLUE}")
// Cucumber 8 glue hints call Class.getDeclaringClass() on every glue class, which throws
// IncompatibleClassChangeError on Kotlin coroutine-inlined classes.
@ConfigurationParameter(key = Constants.GLUE_HINT_ENABLED_PROPERTY_NAME, value = "false")
class CouchdbSsmCucumberTest
