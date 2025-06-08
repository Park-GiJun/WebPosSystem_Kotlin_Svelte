package com.gijun.backend.application.service

import com.gijun.backend.application.port.out.AuditLogRepository
import com.gijun.backend.domain.audit.entities.AuditLog
import com.gijun.backend.domain.audit.enums.AuditActionType
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AuditLogService(
    private val auditLogRepository: AuditLogRepository
) {

    suspend fun getAuditLogs(
        page: Int = 0,
        size: Int = 20,
        tableName: String? = null,
        actionType: AuditActionType? = null,
        userId: String? = null,
        userName: String? = null,
        searchTerm: String? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): AuditLogListResponse {
        
        val actualStartDate = startDate ?: LocalDateTime.now().minusDays(30)
        val actualEndDate = endDate ?: LocalDateTime.now()
        
        val auditLogs = auditLogRepository.findWithFilters(
            tableName = tableName,
            actionType = actionType,
            userId = userId,
            userName = userName,
            searchTerm = searchTerm,
            startDate = actualStartDate,
            endDate = actualEndDate,
            page = page,
            size = size
        ).toList()
        
        val totalCount = auditLogRepository.countWithFilters(
            tableName = tableName,
            actionType = actionType,
            userId = userId,
            userName = userName,
            searchTerm = searchTerm,
            startDate = actualStartDate,
            endDate = actualEndDate
        )
        
        return AuditLogListResponse(
            auditLogs = auditLogs.map { it.toDto() },
            totalCount = totalCount,
            page = page,
            size = size,
            totalPages = (totalCount + size - 1) / size
        )
    }

    suspend fun getAuditLogById(id: Long): AuditLogDto? {
        return auditLogRepository.findById(id)?.toDto()
    }

    suspend fun getRecordHistory(tableName: String, recordId: String): List<AuditLogDto> {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId)
            .toList()
            .map { it.toDto() }
    }

    suspend fun getStatistics(days: Int = 7): AuditStatisticsDto {
        val since = LocalDateTime.now().minusDays(days.toLong())
        
        val actionStats = auditLogRepository.getActionStatistics(since)
        val tableStats = auditLogRepository.getTableStatistics(since)
        val userStats = auditLogRepository.getUserStatistics(since)
        
        return AuditStatisticsDto(
            period = "${days}일",
            actionStatistics = actionStats.mapKeys { it.key.name },
            tableStatistics = tableStats,
            userStatistics = userStats,
            totalEvents = actionStats.values.sum()
        )
    }

    private fun AuditLog.toDto(): AuditLogDto {
        return AuditLogDto(
            id = this.id,
            auditId = this.auditId,
            tableName = this.tableName,
            tableDisplayName = this.getTableDisplayName(),
            recordId = this.recordId,
            actionType = this.actionType.name,
            actionDisplayName = this.getActionDisplayName(),
            oldValues = this.oldValues,
            newValues = this.newValues,
            changedFields = this.changedFields,
            userId = this.userId,
            userName = this.userName,
            ipAddress = this.ipAddress,
            userAgent = this.userAgent,
            sessionId = this.sessionId,
            requestUri = this.requestUri,
            description = this.description,
            createdAt = this.createdAt
        )
    }
}

data class AuditLogListResponse(
    val auditLogs: List<AuditLogDto>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
    val totalPages: Long
)

data class AuditLogDto(
    val id: Long?,
    val auditId: String,
    val tableName: String,
    val tableDisplayName: String,
    val recordId: String,
    val actionType: String,
    val actionDisplayName: String,
    val oldValues: com.fasterxml.jackson.databind.JsonNode?,
    val newValues: com.fasterxml.jackson.databind.JsonNode?,
    val changedFields: com.fasterxml.jackson.databind.JsonNode?,
    val userId: String?,
    val userName: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val sessionId: String?,
    val requestUri: String?,
    val description: String?,
    val createdAt: LocalDateTime
)

data class AuditStatisticsDto(
    val period: String,
    val actionStatistics: Map<String, Long>,
    val tableStatistics: Map<String, Long>,
    val userStatistics: Map<String, Long>,
    val totalEvents: Long
)
