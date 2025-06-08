package com.gijun.backend.domain.audit.enums

enum class AuditActionType {
    INSERT,
    UPDATE,
    DELETE;
    
    companion object {
        fun fromString(value: String): AuditActionType = when (value.uppercase()) {
            "INSERT" -> INSERT
            "UPDATE" -> UPDATE  
            "DELETE" -> DELETE
            else -> throw IllegalArgumentException("Unknown action type: $value")
        }
    }
}
