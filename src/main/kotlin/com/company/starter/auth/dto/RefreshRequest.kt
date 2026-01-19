package com.company.starter.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RefreshRequest(
    @field:NotNull(message = "Refresh token is required")
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)