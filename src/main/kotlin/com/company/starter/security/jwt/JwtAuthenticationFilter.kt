package com.company.starter.security.jwt

import com.company.starter.common.error.ErrorCode
import com.company.starter.common.error.ErrorResponse
import com.company.starter.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.OffsetDateTime
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    private fun writeUnauthorized(
        request: HttpServletRequest,
        response: HttpServletResponse,
        message: String = "Unauthorized"
    ){
        if (response.isCommitted) return
        SecurityContextHolder.clearContext()

        val errorCode = ErrorCode.AUTH_UNAUTHORIZED
        val httpStatus = errorCode.httpStatus

        val body = ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            code = errorCode.name,
            message = message,
            path = request.requestURI,
            details = null
        )

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        objectMapper.writeValue(response.writer, body)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ){

        val authHeader = request.getHeader("Authorization")

        // No Header=> continue (public endpoints)
        if(authHeader.isNullOrBlank()){
            filterChain.doFilter(request, response)
            return
        }

        // 2. Header exists but malformed => 401
        if(!authHeader.startsWith("Bearer ")){
            writeUnauthorized(
                request,
                response,
                "Invalid Authorization header"
            )
            return
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()){
            writeUnauthorized(request, response, "Invalid token")
            return
        }

        try {
            // 3. Token exists but invalid/expired => 401 (do NOT CONTINUE)
            if (!jwtService.isValid(token, expectedType = TokenType.ACCESS)){
                writeUnauthorized(request, response, "Invalid or expired token")
                return
            }
            if (SecurityContextHolder.getContext().authentication == null) {
                val claims = jwtService.parseClaims(token)

                val subject = claims.subject
                if (subject.isNullOrBlank()){
                    writeUnauthorized(request, response, "Invalid token: missing subject")
                    return
                }

                val userId = try {
                    UUID.fromString(subject)
                } catch (ex: IllegalArgumentException){
                    writeUnauthorized(request, response, "Invalid token subject")
                    return
                }

                val tokenVersion = when(val v = claims["ver"]){
                    is Int -> v
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: 0
                    else -> 0
                }

                val user = userRepository.findByIdAndDisabledAtIsNull(userId)
                if (user == null || user.tokenVersion != tokenVersion){
                    writeUnauthorized(request, response, "Invalid token")
                    return
                }

                val roles: List<String> = when(val r = claims["roles"]){
                    is List<*> -> r.filterIsInstance<String>()
                    is String -> listOf(r)
                    else -> listOf(user.role.name)
                }
                val authorities = roles.map {role ->
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

            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            // 4. No silent failure
            log.warn("JWT filter failure: {}", ex.message)
            writeUnauthorized(request, response, "Invalid token")
        }

    }
}
