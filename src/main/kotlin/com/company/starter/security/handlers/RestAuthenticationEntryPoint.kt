package com.company.starter.security.handlers

import com.company.starter.common.error.ErrorCode
import com.company.starter.common.error.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        if (response.isCommitted) return

        val errorCode = ErrorCode.AUTH_UNAUTHORIZED
        val httpStatus = errorCode.httpStatus

        val body = ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            code = errorCode.name,
            message = "Unauthorized",
            path = request.requestURI,
            details = null
        )

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        objectMapper.writeValue(response.writer, body)
    }
}
