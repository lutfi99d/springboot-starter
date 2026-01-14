package com.company.starter.common.error.exceptions

import com.company.starter.common.error.ErrorCode

class NotFoundException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.RESOURCE_NOT_FOUND
) : RuntimeException(message)
