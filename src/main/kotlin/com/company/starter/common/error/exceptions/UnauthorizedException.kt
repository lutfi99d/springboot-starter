package com.company.starter.common.error.exceptions

import com.company.starter.common.error.ErrorCode

class UnauthorizedException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.AUTH_UNAUTHORIZED
) : RuntimeException(message)
