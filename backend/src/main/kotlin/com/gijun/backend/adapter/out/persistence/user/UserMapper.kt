package com.gijun.backend.adapter.out.persistence.user

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val objectMapper: ObjectMapper
) {

    fun toDomain(entity: UserEntity): User {
        val roles = parseRoles(entity.roles)
        val userStatus = parseUserStatus(entity.userStatus)

        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            passwordHash = entity.passwordHash,
            roles = roles,
            userStatus = userStatus,
            organizationId = entity.organizationId,
            organizationType = entity.organizationType,
            lastLoginAt = entity.lastLoginAt,
            failedLoginAttempts = entity.failedLoginAttempts,
            lockedUntil = entity.lockedUntil,
            emailVerifiedAt = entity.emailVerifiedAt,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
            deletedAt = entity.deletedAt,
            deletedBy = entity.deletedBy,
            version = entity.version
        )
    }

    fun toEntity(domain: User): UserEntity {
        val rolesJson = objectMapper.writeValueAsString(domain.roles.map { it.name })

        return UserEntity(
            id = domain.id,
            username = domain.username,
            email = domain.email,
            passwordHash = domain.passwordHash,
            roles = rolesJson,
            userStatus = domain.userStatus.name,
            organizationId = domain.organizationId,
            organizationType = domain.organizationType,
            lastLoginAt = domain.lastLoginAt,
            failedLoginAttempts = domain.failedLoginAttempts,
            lockedUntil = domain.lockedUntil,
            emailVerifiedAt = domain.emailVerifiedAt,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            createdBy = domain.createdBy,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
            deletedAt = domain.deletedAt,
            deletedBy = domain.deletedBy,
            version = domain.version
        )
    }

    private fun parseRoles(rolesJson: String): Set<UserRole> {
        return try {
            val roleNames: List<String> = objectMapper.readValue(
                rolesJson,
                object : TypeReference<List<String>>() {}
            )
            roleNames.mapNotNull {
                try { UserRole.valueOf(it) } catch (e: IllegalArgumentException) { null }
            }.toSet()
        } catch (e: Exception) {
            setOf(UserRole.USER) // 기본값
        }
    }

    private fun parseUserStatus(statusString: String): UserStatus {
        return try {
            UserStatus.valueOf(statusString)
        } catch (e: IllegalArgumentException) {
            UserStatus.ACTIVE // 기본값
        }
    }
}