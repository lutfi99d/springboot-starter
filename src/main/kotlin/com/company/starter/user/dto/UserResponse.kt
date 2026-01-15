package com.company.starter.user.dto

import com.company.starter.user.model.Role
import java.time.OffsetDateTime

data class UserResponse(
    val id: Long,
    val email: String,
    val role: Role,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
