package ssm.bdd.spring.autoconfigure

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext

class ApplicationContextBuilder {

    fun create(
        types: Array<Class<*>>, profile: Array<String> = emptyArray(), config: Map<String, String>
    ): GenericApplicationContext {
        val withSources = types.fold(SpringApplicationBuilder()) { builder, type -> builder.sources(type) }
        val withProfiles = profile.fold(withSources) { builder, name -> builder.profiles(name) }
        return withProfiles
            .properties(mapOf("server.port" to "0") + config)
            .run() as GenericApplicationContext
    }

    @EnableAutoConfiguration
    @Configuration
    class SimpleConfiguration
}
