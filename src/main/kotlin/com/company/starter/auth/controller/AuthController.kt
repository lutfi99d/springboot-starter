package com.company.starter.auth.controller

import com.company.starter.auth.dto.*
import com.company.starter.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(req.email, req.password))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(req.email, req.password))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.refresh(req.refreshToken))
    }

    // Option A: client-side logout (no server action)
    @PostMapping("/logout")
    fun logout(): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(MessageResponse("Logged out"))
    }

    // Option B: invalidate all tokens by bumping token_version
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
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found")

        // We store userId as principal string in the filter
        val principal = auth.principal?.toString()
            ?: throw IllegalStateException("No principal found")

        return try {
            UUID.fromString(principal)
        } catch (_: IllegalArgumentException) {
            throw IllegalStateException("Invalid principal userId")
        }
    }
}
