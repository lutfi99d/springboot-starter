package com.company.starter.user.service

import com.company.starter.common.error.exceptions.NotFoundException
import com.company.starter.common.pagination.PaginationResponse
import com.company.starter.common.pagination.toPaginationResponse
import com.company.starter.user.dto.UserResponse
import com.company.starter.user.model.Role
import com.company.starter.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun listUsers(page: Int, size: Int, sort: String): PaginationResponse<UserResponse> {
        val (sortField, sortDir) = parseSort(sort)

        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.by(sortDir, sortField)
        )

        return userRepository.findAll(pageable)
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

    fun getUserById(id: Long): UserResponse {
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

    fun changeRole(id: Long, newRole: Role): UserResponse {
        val user = userRepository.findByIdAndDisabledAtIsNull(id)
            ?: throw NotFoundException("User not found")

        user.role = newRole
        val saved = userRepository.save(user)

        return UserResponse(
            id = saved.id!!,
            email = saved.email,
            role = saved.role,
            createdAt = saved.createdAt,
            updatedAt = saved.updatedAt
        )
    }

    private fun parseSort(sort: String): Pair<String, Sort.Direction> {
        // sort format: "createdAt,desc" or "email,asc"
        val parts = sort.split(",").map { it.trim() }
        val field = parts.getOrNull(0).takeUnless { it.isNullOrBlank() } ?: "createdAt"
        val dir = when (parts.getOrNull(1)?.lowercase()) {
            "asc" -> Sort.Direction.ASC
            else -> Sort.Direction.DESC
        }
        return field to dir
    }
}