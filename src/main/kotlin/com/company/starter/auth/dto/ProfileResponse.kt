package com.company.starter.auth.dto

import com.company.starter.user.model.Role
import java.time.OffsetDateTime

data class ProfileResponse(
    val id: Long,
    val email: String,
    val role: Role,
    val tokenVersion: Int,
    val disabledAt: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
