package com.company.starter.security.jwt

import com.company.starter.config.AppProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    private val appProperties: AppProperties
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(appProperties.jwt.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateToken(
        subject: String,
        type: TokenType,
        roles: List<String> = emptyList()
    ): String {
        val now = Instant.now()
        val exp = when (type) {
            TokenType.ACCESS -> now.plus(appProperties.jwt.accessExpMinutes, ChronoUnit.MINUTES)
            TokenType.REFRESH -> now.plus(appProperties.jwt.refreshExpDays, ChronoUnit.DAYS)
        }

        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("type", type.name)
            .claim("roles", roles)
            .signWith(secretKey) //
            .compact()
    }

    fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun isValid(token: String, expectedType: TokenType? = null): Boolean {
        val claims = parseClaims(token)
        val tokenType = claims["type"]?.toString()
        if (expectedType != null && tokenType != expectedType.name) return false

        val exp = claims.expiration?.toInstant() ?: return false
        return exp.isAfter(Instant.now())
    }

    fun getSubject(token: String): String =
        parseClaims(token).subject

    fun getRoles(token: String): List<String> {
        val rolesAny = parseClaims(token)["roles"] ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        return when (rolesAny) {
            is List<*> -> rolesAny.filterIsInstance<String>()
            else -> emptyList()
        }
    }
}
