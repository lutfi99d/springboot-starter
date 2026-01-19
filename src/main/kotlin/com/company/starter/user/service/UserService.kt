package com.company.starter.user.service

import com.company.starter.common.error.exceptions.BadRequestException
import com.company.starter.common.error.exceptions.NotFoundException
import com.company.starter.common.pagination.PaginationResponse
import com.company.starter.common.pagination.toPaginationResponse
import com.company.starter.user.dto.UserResponse
import com.company.starter.user.model.Role
import com.company.starter.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val ALLOWED_SORT_FIELDS = setOf("createdAt", "updatedAt", "email", "role")

    fun listUsers(page: Int, size: Int, sort: String): PaginationResponse<UserResponse> {
        val safePage = page.coerceIn(0, 100)
        val safeSize = size.coerceIn(1, 50)

        val (sortField, sortDir) = parseSort(sort)

        val pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(sortDir, sortField)
        )

        return userRepository.findAllByDisabledAtIsNull(pageable)
            .map { u ->
                UserResponse(
                    id = u.id!!,
                    email = u.email,
                    role = u.role,
                    createdAt = u.createdAt,
                    updatedAt = u.updatedAt
                )
            }
            .toPaginationResponse()
    }

    fun getUserById(id: UUID): UserResponse {
        val user = userRepository.findByIdAndDisabledAtIsNull(id)
            ?: throw NotFoundException("User not found")

        return UserResponse(
            id = user.id!!,
            email = user.email,
            role = user.role,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    fun changeRole(id: UUID, newRole: Role): UserResponse {
        val user = userRepository.findByIdAndDisabledAtIsNull(id)
            ?: throw NotFoundException("User not found")

        user.role = newRole

        val now = OffsetDateTime.now()
        user.updatedAt = now
        val saved = userRepository.save(user)

        return UserResponse(
            id = saved.id!!,
            email = saved.email,
            role = saved.role,
            createdAt = saved.createdAt,
            updatedAt = saved.updatedAt
        )
    }

    fun softDeleteUser(id: UUID) {
        val user = userRepository.findByIdAndDisabledAtIsNull(id)
            ?: throw NotFoundException("User not found")

        val now = OffsetDateTime.now()
        user.disabledAt = now
        user.updatedAt = now
        userRepository.save(user)
    }

    private fun parseSort(sort: String): Pair<String, Sort.Direction> {
        val parts = sort.split(",").map { it.trim() }
        val field = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "createdAt"

        if (field !in ALLOWED_SORT_FIELDS) {
            throw BadRequestException("Invalid sort field '$field'. Allowed: ${ALLOWED_SORT_FIELDS.joinToString(", ")}")
        }

        val dir = when (parts.getOrNull(1)?.lowercase()) {
            "asc" -> Sort.Direction.ASC
            "desc" -> Sort.Direction.DESC
            null, "" -> Sort.Direction.DESC
            else -> throw BadRequestException("Invalid sort direction '${parts[1]}'. Use 'asc' or 'desc'")
        }

        return field to dir
    }
}

