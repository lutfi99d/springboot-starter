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
        // validate refresh token
        if (!jwtService.isValid(refreshToken, expectedType = TokenType.REFRESH)) {
            throw BadRequestException("Invalid refresh token")
        }

        val claims = jwtService.parseClaims(refreshToken)


        val userId = try {
            UUID.fromString(claims.subject)
        } catch (_: Exception) {
            throw BadRequestException("Invalid refresh token")
        }

        val tokenVersion = jwtService.getTokenVersion(refreshToken)

        val user = userRepository.findByIdAndDisabledAtIsNull(userId)
            ?: throw NotFoundException("User not found")

        // logoutAll support
        if (user.tokenVersion != tokenVersion) {
            throw BadRequestException("Refresh token has been invalidated")
        }

        return issueTokens(user)
    }

    @Transactional
    fun logoutAll(currentUserId: UUID) {
        val user = userRepository.findByIdAndDisabledAtIsNull(currentUserId)
            ?: throw NotFoundException("User not found")

        user.tokenVersion += 1
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

        val roles = listOf(user.role.name)

        val access = jwtService.generateToken(
            subject = userId.toString(),
            type = TokenType.ACCESS,
            tokenVersion = user.tokenVersion,
            roles = roles
        )

        val refresh = jwtService.generateToken(
            subject = userId.toString(),
            type = TokenType.REFRESH,
            tokenVersion = user.tokenVersion,
            roles = roles
        )

        return AuthResponse(accessToken = access, refreshToken = refresh)
    }
}
