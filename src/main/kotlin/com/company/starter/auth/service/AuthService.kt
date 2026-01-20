package com.company.starter.auth.service

import com.company.starter.auth.dto.AuthResponse
import com.company.starter.auth.dto.ProfileResponse
import com.company.starter.common.error.exceptions.BadRequestException
import com.company.starter.common.error.exceptions.ConflictException
import com.company.starter.common.error.exceptions.NotFoundException
import com.company.starter.security.jwt.JwtService
import com.company.starter.security.jwt.TokenType
import com.company.starter.user.model.Role
import com.company.starter.user.model.User
import com.company.starter.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.company.starter.common.error.exceptions.UnauthorizedException
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional
    fun register(email: String, password: String): AuthResponse {
        val normalizedEmail = email.trim().lowercase()

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw ConflictException("Email already exists")
        }

        val user = User(
            email = normalizedEmail,
            passwordHash = passwordEncoder.encode(password),
            role = Role.USER,
            tokenVersion = 0
        )

        val saved = userRepository.save(user)

        return issueTokens(saved)
    }

    fun login(email: String, password: String): AuthResponse {
        val normalizedEmail = email.trim().lowercase()

        val user = userRepository.findByEmailAndDisabledAtIsNull(normalizedEmail)

            ?: throw BadRequestException("Invalid email or password")

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw BadRequestException("Invalid email or password")
        }

        return issueTokens(user)
    }

    fun refresh(refreshToken: String): AuthResponse {

        if (refreshToken.count { it == '.' } != 2) {
            throw BadRequestException("Invalid refresh token")
        }

        val claims = try {
            jwtService.parseClaims(refreshToken)
        } catch (_: Exception) {
            throw UnauthorizedException("Invalid refresh token")
        }

        // Check type
        val type = claims["type"]?.toString()
        if (type != TokenType.REFRESH.name) {
            throw UnauthorizedException("Invalid refresh token")
        }

        // Check exp
        val exp = claims.expiration?.toInstant() ?: throw UnauthorizedException("Invalid refresh token")
        if (!exp.isAfter(Instant.now())) {
            throw UnauthorizedException("Invalid refresh token")
        }

        val userId = try {
            UUID.fromString(claims.subject)
        } catch (_: Exception) {
            throw BadRequestException("Invalid refresh token")
        }

        val tokenVersion = when (val v = claims["ver"]) {
            is Int -> v
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: 0
            else -> 0
        }

        val user = userRepository.findByIdAndDisabledAtIsNull(userId)
            ?: throw UnauthorizedException("Invalid refresh token")

        if (user.tokenVersion != tokenVersion) {
            throw UnauthorizedException("Refresh token has been invalidated")
        }

        return issueTokens(user)
    }

    @Transactional
    fun logoutAll(currentUserId: UUID) {
        val user = userRepository.findByIdAndDisabledAtIsNull(currentUserId)
            ?: throw NotFoundException("User not found")

        val now = OffsetDateTime.now()
        user.tokenVersion += 1
        user.updatedAt = now

        userRepository.save(user)
    }

    fun profile(currentUserId: UUID): ProfileResponse {
        val user = userRepository.findByIdAndDisabledAtIsNull(currentUserId)
            ?: throw NotFoundException("User not found")

        return ProfileResponse(
            id = user.id!!,
            email = user.email,
            role = user.role,
            updatedAt = user.updatedAt,
            createdAt = user.createdAt,
            disabledAt = user.disabledAt,
            tokenVersion = user.tokenVersion
        )
    }

    private fun issueTokens(user: User): AuthResponse {
        val userId = user.id ?: throw IllegalStateException("User id is null")

        val access = jwtService.generateToken(
            subject = userId.toString(),
            type = TokenType.ACCESS,
            tokenVersion = user.tokenVersion,
            roles = listOf(user.role.name)
        )

        val refresh = jwtService.generateToken(
            subject = userId.toString(),
            type = TokenType.REFRESH,
            tokenVersion = user.tokenVersion,
            roles = emptyList()
        )

        return AuthResponse(
            accessToken = access,
            refreshToken = refresh,
            role = user.role
        )
    }
}
