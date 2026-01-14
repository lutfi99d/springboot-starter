package com.company.starter.common.error.exceptions

import com.company.starter.common.error.ErrorCode

class ForbiddenException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.ACCESS_DENIED
) : RuntimeException(message)
