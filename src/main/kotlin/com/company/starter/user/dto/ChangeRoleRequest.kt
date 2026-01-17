package com.company.starter.user.dto

import com.company.starter.user.model.Role
import jakarta.validation.constraints.NotNull

data class ChangeRoleRequest(
    @field:NotNull(message = "Role is required")
    val role: Role?
)
