package com.company.starter.security.jwt


import com.company.starter.config.AppProperties
import com.company.starter.config.CorsProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class SecurityStartupChecks(
    private val appProperties: AppProperties,
    private val corsProperties: CorsProperties
) {

    @PostConstruct
    fun validateCriticalSecurityConfig() {
        // JWT
        val secret = appProperties.jwt.secret.trim()
        require(secret.isNotBlank()) {
            "JWT_SECRET must be provided (non-blank). Refusing to start."
        }
        require(secret.length >= 32) {
            "JWT_SECRET must be at least 32 characters long. Refusing to start."
        }

        // CORS (extra safety)
        val origins = corsProperties.allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        require(origins.isNotEmpty()) {
            "CORS_ALLOWED_ORIGINS must be provided. Refusing to start."
        }
        require(origins.none { it == "*" }) {
            "CORS_ALLOWED_ORIGINS must not contain '*'. Refusing to start."
        }
    }
}