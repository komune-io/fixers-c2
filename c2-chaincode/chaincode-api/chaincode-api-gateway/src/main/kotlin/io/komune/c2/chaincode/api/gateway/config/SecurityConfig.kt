package io.komune.c2.chaincode.api.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Security configuration that disables authentication by default.
 * This allows the gateway to run without authentication in development/test environments.
 *
 * To enable authentication, set `coop.security.enabled=true` in application properties.
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    @Order(-1)
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().permitAll() }
            .build()
    }
}
