package com.company.starter.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.admin")
@ConditionalOnProperty(
    name =["app.admin-seed.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
data class AdminProperties(
    val email: String,
    val password: String
)
