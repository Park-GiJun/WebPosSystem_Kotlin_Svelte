package com.gijun.backend.domain.common

import java.time.LocalDateTime

interface AuditableEntity {
    val isActive: Boolean
    val createdAt: LocalDateTime
    val createdBy: String?
    val updatedAt: LocalDateTime
    val updatedBy: String?
    val deletedAt: LocalDateTime?
    val deletedBy: String?

    fun isDeleted(): Boolean = deletedAt != null
}