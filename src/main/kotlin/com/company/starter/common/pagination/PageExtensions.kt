package com.company.starter.common.pagination

import org.springframework.data.domain.Page

fun <T> Page<T>.toPaginationResponse(): PaginationResponse<T> =
    PaginationResponse(
        items = content,
        pageIndex = number,
        size = size,
        totalItems = totalElements,
        totalPages = totalPages,
        hasNext = hasNext()
    )