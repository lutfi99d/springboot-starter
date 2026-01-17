package com.company.starter.user.repository

import com.company.starter.user.model.User
import org.apache.el.stream.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
    fun findByIdAndDisabledAtIsNull(id: Long): User?
    fun findByEmailAndDisabledAtIsNull(email: String): User?
    fun findAllByDisabledAtIsNull(pageable: Pageable): Page<User>
    fun existsByIdAndDisabledAtIsNull(id: Long): Boolean
}
