package com.company.starter.security.handlers

import com.company.starter.common.error.ErrorCode
import com.company.starter.common.error.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RestAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        if (response.isCommitted) return
        val errorCode = ErrorCode.ACCESS_DENIED
        val httpStatus = errorCode.httpStatus

        val body = ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            code = errorCode.name,
            message = "Access denied",
            path = request.requestURI,
            details = null
        )

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        objectMapper.writeValue(response.writer, body)
    }
}
