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
    private companion object {
        const val CLAIM_TYPE = "type"
        const val CLAIM_ROLES = "roles"
        const val CLAIM_VERSION = "ver"
    }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(appProperties.jwt.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateToken(
        subject: String,
        type: TokenType,
        tokenVersion: Int,
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
            .claim(CLAIM_TYPE, type.name)
            .claim(CLAIM_ROLES, roles)
            .claim(CLAIM_VERSION, tokenVersion)
            .signWith(secretKey)
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
        val tokenType = claims[CLAIM_TYPE]?.toString()
        if (expectedType != null && tokenType != expectedType.name) return false

        val exp = claims.expiration?.toInstant() ?: return false
        return exp.isAfter(Instant.now())
    }

    fun getSubject(token: String): String =
        parseClaims(token).subject

    fun getRoles(token: String): List<String> {
        val rolesAny = parseClaims(token)[CLAIM_ROLES] ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        return when (rolesAny) {
            is List<*> -> rolesAny.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    fun getTokenVersion(token: String): Int {
        val ver = parseClaims(token)[CLAIM_VERSION] ?: return 0
        return when (ver) {
            is Int -> ver
            is Number -> ver.toInt()
            is String -> ver.toIntOrNull() ?: 0
            else -> 0
        }
    }

}
