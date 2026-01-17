package com.company.starter.user.dto

import com.company.starter.user.model.Role
import java.time.OffsetDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val role: Role,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
