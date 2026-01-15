package com.company.starter.security.jwt

import com.company.starter.user.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        try {
            if (!jwtService.isValid(token, expectedType = TokenType.ACCESS)) {
                filterChain.doFilter(request, response)
                return
            }

            if (SecurityContextHolder.getContext().authentication == null) {

                val claims = jwtService.parseClaims(token)

                val subject = claims.subject ?: run {
                    filterChain.doFilter(request, response)
                    return
                }

                val userId = subject.toLongOrNull() ?: run {
                    filterChain.doFilter(request, response)
                    return
                }

                val tokenVersion = when (val v = claims["ver"]) {
                    is Int -> v
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: 0
                    else -> 0
                }

                val user = userRepository.findById(userId).orElse(null)
                if (user == null || user.tokenVersion != tokenVersion) {
                    filterChain.doFilter(request, response)
                    return
                }

                val roles: List<String> = when (val r = claims["roles"]) {
                    is List<*> -> r.filterIsInstance<String>()
                    else -> listOf(user.role.name)
                }

                val authorities = roles.map { role ->
                    val normalized = if (role.startsWith("ROLE_")) role else "ROLE_$role"
                    SimpleGrantedAuthority(normalized)
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    authorities
                ).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (_: Exception) {
        }

        filterChain.doFilter(request, response)
    }
}
