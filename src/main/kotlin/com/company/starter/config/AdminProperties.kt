package com.company.starter.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.admin")
data class AdminProperties(
    val email: String,
    val password: String
)
