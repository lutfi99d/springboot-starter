package com.company.starter.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated


@Validated
@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    @field:NotBlank(message = "CORS allowed-origins must be provided")
    val allowedOrigins: String
)
