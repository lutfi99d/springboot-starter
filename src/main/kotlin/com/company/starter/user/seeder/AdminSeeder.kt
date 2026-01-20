package com.company.starter.user.seeder

import com.company.starter.config.AdminProperties
import com.company.starter.user.model.Role
import com.company.starter.user.model.User
import com.company.starter.user.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.context.annotation.Profile
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@ConditionalOnProperty(
    name = ["app.admin-seed.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@Profile("local","dev")
@Component
class AdminSeeder(
    private val adminProperties: AdminProperties,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(AdminProperties::class.java)

    @Transactional
    override fun run(vararg args: String?) {

        log.info("AdminSeeder starting...")

        val email = adminProperties.email.trim().lowercase()
        val password = adminProperties.password.trim()

        if (email.isBlank() || password.isBlank()){
            log.warn("AdminSeeder skipped: email or password is blank.")
            return
        }

        if (userRepository.existsByEmail(email)){
            log.warn("AdminSeeder skipped: email already exists.")
            return
        }

        val admin = User(
            email = email,
            passwordHash = passwordEncoder.encode(password),
            role = Role.ADMIN,
            tokenVersion = 0
        )

        userRepository.save(admin)
        log.info("AdminSeeder: ✅ Admin user created with email={}", email)
    }
}
