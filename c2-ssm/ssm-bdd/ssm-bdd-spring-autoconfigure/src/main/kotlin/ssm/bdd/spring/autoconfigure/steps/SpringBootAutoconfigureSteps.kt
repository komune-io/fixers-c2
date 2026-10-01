package ssm.bdd.spring.autoconfigure.steps

import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder

open class SpringBootAutoconfigureSteps : En {
	companion object {
		const val GLUE = "ssm.bdd.spring"
		var applicationParameters: Map<String, String> = emptyMap()
		var contextBuilder: ReactiveWebApplicationContextRunner? = null
	}

	init {
		Given("The application parameters") { table: DataTable ->
			applicationParameters = table.asMap(String::class.java, String::class.java)
				.entries.associate { (key, value) ->
					requireNotNull(key) to requireNotNull(value) { "Missing value for application parameter '$key'" }
				}
		}
		When("I build a valid spring application context") {
			contextBuilder = ApplicationContextRunnerBuilder()
				.buildContext(applicationParameters)
		}
		Then("Instance of {string} is an injectable bean") { functionName: String ->
			contextBuilder?.run { context ->
				assertThat(context).hasBean(functionName)
			}
		}

		When("I get an empty spring application context") {
			contextBuilder = ApplicationContextRunnerBuilder().buildContext()
		}

		Then("Instance of {string} is not injectable bean") { functionName: String ->
			contextBuilder?.run { context ->
				assertThat(context).doesNotHaveBean(functionName)
			}
		}
	}
}
