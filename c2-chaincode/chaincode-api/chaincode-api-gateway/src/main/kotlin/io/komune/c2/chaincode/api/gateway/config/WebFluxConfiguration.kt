package io.komune.c2.chaincode.api.gateway.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer


@Configuration
@EnableWebFlux
class WebFluxConfiguration : WebFluxConfigurer {
    companion object {
        private const val MAX_AGE = 3600L
        private const val BYTE_COUNT = 10 * 1024 * 1024
    }
    override fun addCorsMappings(corsRegistry: CorsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("*")
                .maxAge(MAX_AGE);
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false)
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().maxInMemorySize(BYTE_COUNT) // Set to 10 MB
    }
}
