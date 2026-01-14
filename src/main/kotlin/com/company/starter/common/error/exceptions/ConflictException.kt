package com.company.starter.common.error.exceptions

import com.company.starter.common.error.ErrorCode

class ConflictException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.RESOURCE_ALREADY_EXISTS
) : RuntimeException(message)
