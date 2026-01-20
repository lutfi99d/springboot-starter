package com.company.starter.auth.dto

import com.company.starter.user.model.Role

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: Role
)
