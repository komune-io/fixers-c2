package ssm.bdd.spring.autoconfigure

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext


class ApplicationContextBuilder {

	fun create(
		types: Array<Class<*>>, profile: Array<String> = emptyArray(), config: Map<String, String>
	): GenericApplicationContext {
		return SpringApplicationBuilder(*types)
			.profiles(*profile)
			.withConfig(config)
			.run() as GenericApplicationContext
	}

	private fun SpringApplicationBuilder.withConfig(config: Map<String, String>): SpringApplicationBuilder {
		val allConfig = buildMap {
			put("server.port", "0")
			putAll(config)
		}
		return properties(
			*allConfig.map { pair ->
				"${pair.key}=${pair.value}"
			}.toTypedArray()
		)
	}

	@EnableAutoConfiguration
	@Configuration
	class SimpleConfiguration


}
