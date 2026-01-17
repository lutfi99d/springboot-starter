package com.company.starter.user.controller

import com.company.starter.common.pagination.PaginationResponse
import com.company.starter.user.dto.UserResponse
import com.company.starter.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UsersController(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt,desc") sort: String
    ): ResponseEntity<PaginationResponse<UserResponse>> {
        return ResponseEntity.ok(userService.listUsers(page, size, sort))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUserById(id))
    }



}
