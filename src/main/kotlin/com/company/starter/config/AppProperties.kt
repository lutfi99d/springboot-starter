package com.company.starter.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
) {
    data class JwtProperties(
        val secret: String,
        val accessExpMinutes: Long,
        val refreshExpDays: Long,
    )
}