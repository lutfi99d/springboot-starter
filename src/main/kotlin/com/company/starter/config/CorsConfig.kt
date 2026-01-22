package com.company.starter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties,
    private val environment: Environment
) {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()

        val origins = corsProperties.allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val isProdLike = environment.activeProfiles.any { it == "prod" || it == "staging" }

        /*
         * Hard security validation (fail-fast)
         */
        if (origins.isEmpty()) {
            throw IllegalStateException(
                "CORS allowed-origins resolved to an empty list. Refusing to start."
            )
        }

        if (origins.any { it == "*" }) {
            throw IllegalStateException(
                "CORS allowed-origins must not contain '*'. Refusing to start."
            )
        }

        if (isProdLike && origins.any {
                !(it.startsWith("http://") || it.startsWith("https://"))
            }) {
            throw IllegalStateException(
                "CORS allowed-origins contains invalid origin(s). Only http/https are allowed in prod/staging."
            )
        }

        config.allowedOrigins = origins
        config.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        )
        config.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Accept"
        )
        config.exposedHeaders = listOf("Authorization")
        config.allowCredentials = true
        config.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
