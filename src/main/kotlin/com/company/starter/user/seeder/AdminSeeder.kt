package com.company.starter.user.seeder

import com.company.starter.config.AdminProperties
import com.company.starter.user.model.Role
import com.company.starter.user.model.User
import com.company.starter.user.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AdminSeeder(
    private val adminProperties: AdminProperties,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val email = adminProperties.email.trim().lowercase()
        val password = adminProperties.password.trim()

        if (email.isBlank() || password.isBlank()) return

        if (userRepository.existsByEmail(email)) return

        val admin = User(
            email = email,
            passwordHash = passwordEncoder.encode(password),
            role = Role.ADMIN,
            tokenVersion = 0
        )

        userRepository.save(admin)
        println("✅ Admin user created: $email")
    }
}
