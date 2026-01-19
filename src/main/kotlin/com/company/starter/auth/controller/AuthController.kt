package com.company.starter.auth.controller

import com.company.starter.auth.dto.*
import com.company.starter.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import com.company.starter.common.error.exceptions.UnauthorizedException
import org.springframework.security.core.Authentication
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register",consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.register(req.email, req.password))

    }

    @PostMapping("/login",consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(req.email, req.password))
    }

    @PostMapping("/refresh", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun refresh(@Valid @RequestBody req: RefreshRequest): ResponseEntity<AuthResponse> {
        val token = requireNotNull(req.refreshToken) { "Refresh token is required" }
        return ResponseEntity.ok(authService.refresh(token))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(MessageResponse("Logged out"))
    }

    @PostMapping("/logout-all")
    fun logoutAll(): ResponseEntity<MessageResponse> {
        val userId = currentUserId()
        authService.logoutAll(userId)
        return ResponseEntity.ok(MessageResponse("Logged out from all sessions"))
    }

    @GetMapping("/profile")
    fun profile(): ResponseEntity<ProfileResponse> {
        val userId = currentUserId()
        return ResponseEntity.ok(authService.profile(userId))
    }
    private fun currentUserId(): UUID {
        val auth: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("Unauthorized")

        if (!auth.isAuthenticated) {
            throw UnauthorizedException("Unauthorized")
        }

        val principal = auth.principal?.toString()
            ?: throw UnauthorizedException("Unauthorized")

        return try {
            UUID.fromString(principal)
        } catch (_: IllegalArgumentException) {
            throw UnauthorizedException("Unauthorized")
        }
    }
}
