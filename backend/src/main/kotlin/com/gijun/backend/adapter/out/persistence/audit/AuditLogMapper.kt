package com.gijun.backend.adapter.out.persistence.audit

import com.gijun.backend.domain.audit.entities.AuditLog
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class AuditLogMapper(
    private val objectMapper: ObjectMapper
) {
    
    fun toDomain(entity: AuditLogEntity): AuditLog {
        return AuditLog(
            id = entity.id,
            auditId = entity.auditId,
            tableName = entity.tableName,
            recordId = entity.recordId,
            actionType = entity.actionType,
            oldValues = entity.oldValues?.let { parseJson(it) },
            newValues = entity.newValues?.let { parseJson(it) },
            changedFields = entity.changedFields?.let { parseJson(it) },
            userId = entity.userId,
            userName = entity.userName,
            ipAddress = entity.ipAddress,
            userAgent = entity.userAgent,
            sessionId = entity.sessionId,
            requestUri = entity.requestUri,
            description = entity.description,
            createdAt = entity.createdAt
        )
    }
    
    fun toEntity(domain: AuditLog): AuditLogEntity {
        return AuditLogEntity(
            id = domain.id,
            auditId = domain.auditId,
            tableName = domain.tableName,
            recordId = domain.recordId,
            actionType = domain.actionType,
            oldValues = domain.oldValues?.let { objectMapper.writeValueAsString(it) },
            newValues = domain.newValues?.let { objectMapper.writeValueAsString(it) },
            changedFields = domain.changedFields?.let { objectMapper.writeValueAsString(it) },
            userId = domain.userId,
            userName = domain.userName,
            ipAddress = domain.ipAddress,
            userAgent = domain.userAgent,
            sessionId = domain.sessionId,
            requestUri = domain.requestUri,
            description = domain.description,
            createdAt = domain.createdAt
        )
    }
    
    private fun parseJson(jsonString: String): JsonNode? {
        return try {
            objectMapper.readTree(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    fun toDomainList(entities: List<AuditLogEntity>): List<AuditLog> {
        return entities.map { toDomain(it) }
    }
}
