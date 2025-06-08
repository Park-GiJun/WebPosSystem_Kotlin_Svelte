package com.gijun.backend.adapter.out.persistence.audit

import com.gijun.backend.application.port.out.AuditLogRepository
import com.gijun.backend.domain.audit.entities.AuditLog
import com.gijun.backend.domain.audit.enums.AuditActionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AuditLogRepositoryImpl(
    private val r2dbcRepository: AuditLogR2dbcRepository,
    private val auditLogMapper: AuditLogMapper
) : AuditLogRepository {

    override suspend fun save(auditLog: AuditLog): AuditLog {
        val entity = auditLogMapper.toEntity(auditLog)
        val savedEntity = r2dbcRepository.save(entity)
        return auditLogMapper.toDomain(savedEntity)
    }

    override suspend fun findById(id: Long): AuditLog? {
        return r2dbcRepository.findById(id)?.let { auditLogMapper.toDomain(it) }
    }

    override suspend fun findAllWithPagination(page: Int, size: Int): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findAllWithPagination(size, offset)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun countAll(): Long {
        return r2dbcRepository.countAll()
    }

    override suspend fun findByTableName(tableName: String, page: Int, size: Int): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findByTableName(tableName, size, offset)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun countByTableName(tableName: String): Long {
        return r2dbcRepository.countByTableName(tableName)
    }

    override suspend fun findByUserId(userId: String, page: Int, size: Int): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findByUserId(userId, size, offset)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun findByUserName(userName: String, page: Int, size: Int): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findByUserName(userName, size, offset)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun findByActionType(actionType: AuditActionType, page: Int, size: Int): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findByActionType(actionType, size, offset)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        page: Int,
        size: Int
    ): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findByDateRange(startDate, endDate, size, offset)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun countByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Long {
        return r2dbcRepository.countByDateRange(startDate, endDate)
    }

    override suspend fun findByTableNameAndRecordId(tableName: String, recordId: String): Flow<AuditLog> {
        return r2dbcRepository.findByTableNameAndRecordId(tableName, recordId)
            .map { auditLogMapper.toDomain(it) }
    }

    override suspend fun findWithFilters(
        tableName: String?,
        actionType: AuditActionType?,
        userId: String?,
        userName: String?,
        searchTerm: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        page: Int,
        size: Int
    ): Flow<AuditLog> {
        val offset = page * size
        return r2dbcRepository.findWithFilters(
            tableName, actionType, userId, userName, searchTerm,
            startDate, endDate, size, offset
        ).map { auditLogMapper.toDomain(it) }
    }

    override suspend fun countWithFilters(
        tableName: String?,
        actionType: AuditActionType?,
        userId: String?,
        userName: String?,
        searchTerm: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long {
        return r2dbcRepository.countWithFilters(
            tableName, actionType, userId, userName, searchTerm,
            startDate, endDate
        )
    }

    override suspend fun getActionStatistics(since: LocalDateTime): Map<AuditActionType, Long> {
        val stats = r2dbcRepository.getActionStatistics(since).toList()
        return stats.associate { 
            AuditActionType.fromString(it["action_type"] as String) to (it["count"] as Number).toLong()
        }
    }

    override suspend fun getTableStatistics(since: LocalDateTime): Map<String, Long> {
        val stats = r2dbcRepository.getTableStatistics(since).toList()
        return stats.associate { 
            it["table_name"] as String to (it["count"] as Number).toLong()
        }
    }

    override suspend fun getUserStatistics(since: LocalDateTime): Map<String, Long> {
        val stats = r2dbcRepository.getUserStatistics(since).toList()
        return stats.associate { 
            it["user_name"] as String to (it["count"] as Number).toLong()
        }
    }
}
