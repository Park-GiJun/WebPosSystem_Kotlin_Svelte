package com.gijun.backend.domain.user.entity

import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val roles: Set<UserRole> = setOf(UserRole.USER),
    val userStatus: UserStatus = UserStatus.ACTIVE,

    // Security & Login tracking
    val lastLoginAt: LocalDateTime? = null,
    val failedLoginAttempts: Int = 0,
    val lockedUntil: LocalDateTime? = null,
    val emailVerifiedAt: LocalDateTime? = null,

    // Audit fields
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val updatedBy: String? = null,
    val deletedAt: LocalDateTime? = null,
    val deletedBy: String? = null,

    // Version for optimistic locking
    val version: Long = 0
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length in 3..50) { "Username must be between 3 and 50 characters" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.length <= 320) { "Email cannot exceed 320 characters" }
        require(passwordHash.isNotBlank()) { "Password hash cannot be blank" }
    }

    // Role related methods
    fun hasRole(role: UserRole): Boolean = roles.contains(role)
    fun isAdmin(): Boolean = hasRole(UserRole.SYSTEM_ADMIN) || hasRole(UserRole.SUPER_ADMIN)
    fun isManager(): Boolean = roles.any { it.isManager() }

    // Status related methods
    fun isOperational(): Boolean = userStatus.isOperational() && isActive
    fun canLogin(): Boolean = userStatus.canLogin() && isActive && !isLocked()
    fun isLocked(): Boolean = lockedUntil?.isAfter(LocalDateTime.now()) == true
    fun isEmailVerified(): Boolean = emailVerifiedAt != null
    fun isDeleted(): Boolean = deletedAt != null

    // State change methods
    fun addRole(role: UserRole, updatedBy: String): User =
        copy(
            roles = roles + role,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun removeRole(role: UserRole, updatedBy: String): User {
        require(roles.size > 1) { "User must have at least one role" }
        return copy(
            roles = roles - role,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )
    }

    fun changeStatus(newStatus: UserStatus, updatedBy: String): User =
        copy(
            userStatus = newStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun activate(updatedBy: String): User =
        copy(
            isActive = true,
            userStatus = UserStatus.ACTIVE,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun deactivate(updatedBy: String): User =
        copy(
            isActive = false,
            userStatus = UserStatus.INACTIVE,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun suspend(reason: String? = null, updatedBy: String): User =
        changeStatus(UserStatus.SUSPENDED, updatedBy)

    fun lock(until: LocalDateTime, updatedBy: String): User =
        copy(
            userStatus = UserStatus.LOCKED,
            lockedUntil = until,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun unlock(updatedBy: String): User =
        copy(
            userStatus = UserStatus.ACTIVE,
            lockedUntil = null,
            failedLoginAttempts = 0,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun recordLoginAttempt(success: Boolean, updatedBy: String): User {
        return if (success) {
            copy(
                lastLoginAt = LocalDateTime.now(),
                failedLoginAttempts = 0,
                updatedAt = LocalDateTime.now(),
                updatedBy = updatedBy,
                version = version + 1
            )
        } else {
            val newFailedAttempts = failedLoginAttempts + 1
            val shouldLock = newFailedAttempts >= 5

            if (shouldLock) {
                lock(
                    until = LocalDateTime.now().plusMinutes(30),
                    updatedBy = updatedBy
                ).copy(failedLoginAttempts = newFailedAttempts)
            } else {
                copy(
                    failedLoginAttempts = newFailedAttempts,
                    updatedAt = LocalDateTime.now(),
                    updatedBy = updatedBy,
                    version = version + 1
                )
            }
        }
    }

    fun verifyEmail(updatedBy: String): User =
        copy(
            emailVerifiedAt = LocalDateTime.now(),
            userStatus = if (userStatus == UserStatus.PENDING_VERIFICATION) UserStatus.ACTIVE else userStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = version + 1
        )

    fun delete(deletedBy: String): User =
        copy(
            isActive = false,
            userStatus = UserStatus.INACTIVE,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy,
            version = version + 1
        )
}
