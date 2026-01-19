package com.company.starter.common.error

import com.company.starter.common.error.exceptions.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.OffsetDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map {
            FieldErrorResponse(
                field = it.field,
                message = it.defaultMessage ?: "Invalid value"
            )
        }

        return buildError(
            errorCode = ErrorCode.VALIDATION_ERROR,
            message = "Validation failed",
            request = request,
            details = details
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.BAD_REQUEST,
            message = "Invalid parameter: ${ex.name}",
            request = request
        )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE,
            message = "Unsupported media type. Please use Content-Type: application/json",
            request = request
        )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.METHOD_NOT_ALLOWED,
            message = ex.message ?: "Method not allowed",
            request = request
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException, request: HttpServletRequest) =
        buildError(ex.errorCode, ex.message, request)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, request: HttpServletRequest) =
        buildError(ex.errorCode, ex.message, request)

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException, request: HttpServletRequest) =
        buildError(ex.errorCode, ex.message, request)

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException, request: HttpServletRequest) =
        buildError(ex.errorCode, ex.message, request)

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException, request: HttpServletRequest) =
        buildError(ex.errorCode, ex.message, request)

    @ExceptionHandler(AccessDeniedException::class)
    fun handleSpringAccessDenied(ex: AccessDeniedException, request: HttpServletRequest) =
        buildError(ErrorCode.ACCESS_DENIED, "Access denied", request)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.INTERNAL_ERROR,
            message = "Unexpected error occurred",
            request = request
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.BAD_REQUEST,
            message = "Request body is missing or invalid",
            request = request
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.RESOURCE_ALREADY_EXISTS,
            message = "Resource already exists",
            request = request
        )
    }
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildError(
            errorCode = ErrorCode.BAD_REQUEST,
            message = ex.message ?: "Invalid request",
            request = request
        )
    }

    private fun buildError(
        errorCode: ErrorCode,
        message: String,
        request: HttpServletRequest,
        details: List<FieldErrorResponse>? = null
    ): ResponseEntity<ErrorResponse> {
        val httpStatus: HttpStatus = errorCode.httpStatus

        val body = ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            code = errorCode.name,
            message = message,
            path = request.requestURI,
            details = details
        )

        return ResponseEntity.status(httpStatus).body(body)
    }
}
