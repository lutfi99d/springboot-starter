package com.company.starter.auth.dto

import com.company.starter.user.model.Role

data class ProfileResponse(
    val id: Long,
    val email: String,
    val role: Role
)
