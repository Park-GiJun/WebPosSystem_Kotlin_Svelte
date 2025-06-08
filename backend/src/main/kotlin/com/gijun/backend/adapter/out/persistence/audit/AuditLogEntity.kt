package com.gijun.backend.adapter.out.persistence.audit

import com.gijun.backend.domain.audit.enums.AuditActionType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("audit_logs")
data class AuditLogEntity(
    @Id
    val id: Long? = null,
    
    @Column("audit_id")
    val auditId: String,
    
    @Column("table_name")
    val tableName: String,
    
    @Column("record_id")
    val recordId: String,
    
    @Column("action")
    val actionType: AuditActionType,
    
    @Column("old_values")
    val oldValues: String? = null,
    
    @Column("new_values")
    val newValues: String? = null,
    
    @Column("changed_fields")
    val changedFields: String? = null,
    
    @Column("user_id")
    val userId: String? = null,
    
    @Column("user_name")
    val userName: String? = null,
    
    @Column("ip_address")
    val ipAddress: String? = null,
    
    @Column("user_agent")
    val userAgent: String? = null,
    
    @Column("session_id")
    val sessionId: String? = null,
    
    @Column("request_uri")
    val requestUri: String? = null,
    
    @Column("description")
    val description: String? = null,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
