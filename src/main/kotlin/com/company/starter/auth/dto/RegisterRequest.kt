package com.company.starter.auth.dto

import jakarta.validation.constraints.*


data class RegisterRequest(
    @field:Email(message = "Invalid email")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    val password: String
)
