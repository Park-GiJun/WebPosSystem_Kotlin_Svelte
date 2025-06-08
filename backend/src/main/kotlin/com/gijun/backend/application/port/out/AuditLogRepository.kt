package com.gijun.backend.application.port.out

import com.gijun.backend.domain.audit.entities.AuditLog
import com.gijun.backend.domain.audit.enums.AuditActionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AuditLogRepository {

    // 기본 CRUD
    suspend fun save(auditLog: AuditLog): AuditLog
    suspend fun findById(id: Long): AuditLog?

    // 페이징 조회
    suspend fun findAllWithPagination(page: Int, size: Int): Flow<AuditLog>
    suspend fun countAll(): Long

    // 필터링 조회
    suspend fun findByTableName(tableName: String, page: Int, size: Int): Flow<AuditLog>
    suspend fun countByTableName(tableName: String): Long
    
    suspend fun findByUserId(userId: String, page: Int, size: Int): Flow<AuditLog>
    suspend fun findByUserName(userName: String, page: Int, size: Int): Flow<AuditLog>
    
    suspend fun findByActionType(actionType: AuditActionType, page: Int, size: Int): Flow<AuditLog>
    
    suspend fun findByDateRange(
        startDate: LocalDateTime, 
        endDate: LocalDateTime, 
        page: Int, 
        size: Int
    ): Flow<AuditLog>
    suspend fun countByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Long

    // 특정 레코드의 변경 이력
    suspend fun findByTableNameAndRecordId(tableName: String, recordId: String): Flow<AuditLog>

    // 복합 조건 검색
    suspend fun findWithFilters(
        tableName: String? = null,
        actionType: AuditActionType? = null,
        userId: String? = null,
        userName: String? = null,
        searchTerm: String? = null,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        page: Int,
        size: Int
    ): Flow<AuditLog>

    suspend fun countWithFilters(
        tableName: String? = null,
        actionType: AuditActionType? = null,
        userId: String? = null,
        userName: String? = null,
        searchTerm: String? = null,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long

    // 통계
    suspend fun getActionStatistics(since: LocalDateTime): Map<AuditActionType, Long>
    suspend fun getTableStatistics(since: LocalDateTime): Map<String, Long>
    suspend fun getUserStatistics(since: LocalDateTime): Map<String, Long>
}
