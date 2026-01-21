package com.company.starter.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated


@Validated
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
) {
    data class JwtProperties(

        @field:NotBlank(message = "JWT secret must be provided")
        @field:Size(
            min = 32,
            message = "JWT secret must be at least 32 characters long"
        )
        val secret: String,
        val accessExpMinutes: Long,
        val refreshExpDays: Long,
    )
}