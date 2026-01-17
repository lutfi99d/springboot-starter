package com.company.starter.auth.dto

import com.company.starter.user.model.Role
import java.time.OffsetDateTime
import java.util.UUID

data class ProfileResponse(
    val id: UUID,
    val email: String,
    val role: Role,
    val tokenVersion: Int,
    val disabledAt: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
