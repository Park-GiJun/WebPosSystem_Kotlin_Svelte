package com.gijun.backend.adapter.out.persistence.audit

import com.gijun.backend.domain.audit.enums.AuditActionType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AuditLogR2dbcRepository : CoroutineCrudRepository<AuditLogEntity, Long> {

    // 기본 조회
    @Query("SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllWithPagination(limit: Int, offset: Int): Flow<AuditLogEntity>
    
    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun countAll(): Long

    // 테이블별 조회
    @Query("SELECT * FROM audit_logs WHERE table_name = :tableName ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByTableName(tableName: String, limit: Int, offset: Int): Flow<AuditLogEntity>
    
    @Query("SELECT COUNT(*) FROM audit_logs WHERE table_name = :tableName")
    suspend fun countByTableName(tableName: String): Long

    // 사용자별 조회
    @Query("SELECT * FROM audit_logs WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByUserId(userId: String, limit: Int, offset: Int): Flow<AuditLogEntity>
    
    @Query("SELECT * FROM audit_logs WHERE user_name = :userName ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByUserName(userName: String, limit: Int, offset: Int): Flow<AuditLogEntity>

    // 액션 타입별 조회
    @Query("SELECT * FROM audit_logs WHERE action_type = :actionType ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByActionType(actionType: AuditActionType, limit: Int, offset: Int): Flow<AuditLogEntity>

    // 날짜 범위 조회
    @Query("SELECT * FROM audit_logs WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int, offset: Int): Flow<AuditLogEntity>
    
    @Query("SELECT COUNT(*) FROM audit_logs WHERE created_at BETWEEN :startDate AND :endDate")
    suspend fun countByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Long

    // 특정 레코드 조회
    @Query("SELECT * FROM audit_logs WHERE table_name = :tableName AND record_id = :recordId ORDER BY created_at DESC")
    fun findByTableNameAndRecordId(tableName: String, recordId: String): Flow<AuditLogEntity>

    // 복합 조건 검색
    @Query("""
        SELECT * FROM audit_logs 
        WHERE (:tableName IS NULL OR table_name = :tableName)
        AND (:actionType IS NULL OR action_type = :actionType)
        AND (:userId IS NULL OR user_id = :userId)
        AND (:userName IS NULL OR user_name LIKE CONCAT('%', :userName, '%'))
        AND (:searchTerm IS NULL OR 
             description LIKE CONCAT('%', :searchTerm, '%') OR 
             record_id LIKE CONCAT('%', :searchTerm, '%'))
        AND created_at BETWEEN :startDate AND :endDate
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun findWithFilters(
        tableName: String?,
        actionType: AuditActionType?,
        userId: String?,
        userName: String?,
        searchTerm: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        limit: Int,
        offset: Int
    ): Flow<AuditLogEntity>

    @Query("""
        SELECT COUNT(*) FROM audit_logs 
        WHERE (:tableName IS NULL OR table_name = :tableName)
        AND (:actionType IS NULL OR action_type = :actionType)
        AND (:userId IS NULL OR user_id = :userId)
        AND (:userName IS NULL OR user_name LIKE CONCAT('%', :userName, '%'))
        AND (:searchTerm IS NULL OR 
             description LIKE CONCAT('%', :searchTerm, '%') OR 
             record_id LIKE CONCAT('%', :searchTerm, '%'))
        AND created_at BETWEEN :startDate AND :endDate
    """)
    suspend fun countWithFilters(
        tableName: String?,
        actionType: AuditActionType?,
        userId: String?,
        userName: String?,
        searchTerm: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long

    // 통계 조회
    @Query("""
        SELECT action_type, COUNT(*) as count 
        FROM audit_logs 
        WHERE created_at >= :since 
        GROUP BY action_type
    """)
    fun getActionStatistics(since: LocalDateTime): Flow<Map<String, Any>>

    @Query("""
        SELECT table_name, COUNT(*) as count 
        FROM audit_logs 
        WHERE created_at >= :since 
        GROUP BY table_name 
        ORDER BY count DESC 
        LIMIT 10
    """)
    fun getTableStatistics(since: LocalDateTime): Flow<Map<String, Any>>

    @Query("""
        SELECT user_name, COUNT(*) as count 
        FROM audit_logs 
        WHERE created_at >= :since AND user_name IS NOT NULL
        GROUP BY user_name 
        ORDER BY count DESC 
        LIMIT 10
    """)
    fun getUserStatistics(since: LocalDateTime): Flow<Map<String, Any>>
}
