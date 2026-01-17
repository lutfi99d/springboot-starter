package com.company.starter.user.repository

import com.company.starter.user.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
    fun findByIdAndDisabledAtIsNull(id: UUID): User?
    fun findByEmailAndDisabledAtIsNull(email: String): User?
    fun findAllByDisabledAtIsNull(pageable: Pageable): Page<User>
    fun existsByIdAndDisabledAtIsNull(id: UUID): Boolean
}
