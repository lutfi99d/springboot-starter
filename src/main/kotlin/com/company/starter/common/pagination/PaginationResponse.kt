package com.company.starter.common.pagination

data class PaginationResponse<T>(
    val items: List<T>,
    val pageIndex: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean
)
