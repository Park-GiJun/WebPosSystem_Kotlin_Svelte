package com.gijun.backend.domain.audit.entities

import com.gijun.backend.domain.audit.enums.AuditActionType
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

data class AuditLog(
    val id: Long? = null,
    val auditId: String,
    val tableName: String,
    val recordId: String,
    val actionType: AuditActionType,
    val oldValues: JsonNode? = null,
    val newValues: JsonNode? = null,
    val changedFields: JsonNode? = null,
    val userId: String? = null,
    val userName: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val sessionId: String? = null,
    val requestUri: String? = null,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    fun isUserAction(): Boolean = actionType in setOf(
        AuditActionType.INSERT, 
        AuditActionType.UPDATE, 
        AuditActionType.DELETE
    )
    
    fun hasChanges(): Boolean = changedFields != null && !changedFields.isEmpty
    
    fun getTableDisplayName(): String = when (tableName) {
        "users" -> "사용자"
        "stores" -> "매장"
        "permissions" -> "권한"
        "headquarters" -> "본사"
        "pos_systems" -> "POS 시스템"
        "menus" -> "메뉴"
        else -> tableName
    }
    
    fun getActionDisplayName(): String = when (actionType) {
        AuditActionType.INSERT -> "생성"
        AuditActionType.UPDATE -> "수정"
        AuditActionType.DELETE -> "삭제"
    }
    
    companion object {
        fun create(
            tableName: String,
            recordId: String,
            actionType: AuditActionType,
            userId: String? = null,
            userName: String? = null,
            description: String? = null,
            oldValues: JsonNode? = null,
            newValues: JsonNode? = null,
            changedFields: JsonNode? = null,
            ipAddress: String? = null,
            userAgent: String? = null,
            sessionId: String? = null,
            requestUri: String? = null
        ): AuditLog {
            return AuditLog(
                auditId = java.util.UUID.randomUUID().toString(),
                tableName = tableName,
                recordId = recordId,
                actionType = actionType,
                oldValues = oldValues,
                newValues = newValues,
                changedFields = changedFields,
                userId = userId,
                userName = userName,
                ipAddress = ipAddress,
                userAgent = userAgent,
                sessionId = sessionId,
                requestUri = requestUri,
                description = description
            )
        }
    }
}
