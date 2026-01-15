package com.company.starter.security.jwt

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
    private val jwtService: JwtService
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
            // Validate ACCESS token only (refresh token should not authenticate requests)
            val valid = jwtService.isValid(token, expectedType = TokenType.ACCESS)
            if (!valid) {
                filterChain.doFilter(request, response)
                return
            }

            // Avoid overriding an existing authentication
            if (SecurityContextHolder.getContext().authentication == null) {
                val subject = jwtService.getSubject(token)
                val roles = jwtService.getRoles(token)

                val authorities = roles
                    .map { role ->
                        val normalized = if (role.startsWith("ROLE_")) role else "ROLE_$role"
                        SimpleGrantedAuthority(normalized)
                    }

                val authentication = UsernamePasswordAuthenticationToken(subject, null, authorities).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (_: Exception) {
            // Intentionally ignore parsing/validation errors here.
            // Protected endpoints will return 401 via AuthenticationEntryPoint.
        }

        filterChain.doFilter(request, response)
    }
}
