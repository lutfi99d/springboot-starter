package com.company.starter.common.pagination

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun buildPageable(
    page: Int,
    size: Int,
    sort: String?
): PageRequest {
    val safePage = page.coerceAtLeast(0)
    val safeSize = size.coerceIn(1, 100)

    val sortObj = parseSort(sort)
    return PageRequest.of(safePage, safeSize, sortObj)
}

private fun parseSort(sort: String?): Sort {
    if (sort.isNullOrBlank()) return Sort.by(Sort.Order.desc("createdAt"))

    val parts = sort.split(",").map { it.trim() }
    val field = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "createdAt"
    val dir = parts.getOrNull(1)?.lowercase()

    return if (dir == "asc") Sort.by(Sort.Order.asc(field))
    else Sort.by(Sort.Order.desc(field))
}
