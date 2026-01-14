package com.company.starter.common.error.exceptions

import com.company.starter.common.error.ErrorCode

class BadRequestException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.BAD_REQUEST
) : RuntimeException(message)
