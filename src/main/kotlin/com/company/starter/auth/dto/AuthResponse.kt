package com.company.starter.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)
