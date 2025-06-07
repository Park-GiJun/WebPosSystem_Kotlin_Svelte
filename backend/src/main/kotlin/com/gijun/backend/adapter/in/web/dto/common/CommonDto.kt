package com.gijun.backend.adapter.`in`.web.dto.common

import java.time.LocalDateTime

// Common Response DTOs
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ErrorResponse(
    val error: String,
    val message: String,
    val path: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: Map<String, Any>? = null
)

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long
        ): PagedResponse<T> {
            val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
            return PagedResponse(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                first = page == 0,
                last = page >= totalPages - 1,
                empty = content.isEmpty()
            )
        }
    }
}

data class ValidationErrorResponse(
    val error: String = "Validation Error",
    val message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)

// Health Check DTOs
data class HealthResponse(
    val status: String, // UP, DOWN, UNKNOWN
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: Map<String, Any> = emptyMap()
)

data class HealthDetail(
    val status: String,
    val details: Map<String, Any> = emptyMap()
)

// Sort and Filter DTOs
data class SortRequest(
    val field: String,
    val direction: String = "ASC" // ASC, DESC
)

data class FilterRequest(
    val field: String,
    val operator: String, // EQUALS, CONTAINS, GREATER_THAN, etc.
    val value: Any
)

data class SearchRequest(
    val query: String?,
    val page: Int = 0,
    val size: Int = 20,
    val sorts: List<SortRequest> = emptyList(),
    val filters: List<FilterRequest> = emptyList()
)

// Audit DTOs
data class AuditLogDto(
    val id: Long,
    val tableName: String,
    val recordId: String,
    val action: String, // INSERT, UPDATE, DELETE
    val oldValues: Map<String, Any>?,
    val newValues: Map<String, Any>?,
    val changedFields: List<String>,
    val userId: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: LocalDateTime
)

// File Upload DTOs
data class FileUploadResponse(
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val contentType: String,
    val uploadedAt: LocalDateTime = LocalDateTime.now()
)

data class FileDownloadResponse(
    val fileName: String,
    val contentType: String,
    val content: ByteArray
)
