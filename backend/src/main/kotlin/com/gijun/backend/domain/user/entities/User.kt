package com.gijun.backend.domain.user.entities

import com.gijun.backend.domain.user.vo.*
import com.gijun.backend.domain.user.enums.*
import com.gijun.backend.domain.user.events.*
import com.gijun.backend.domain.common.AuditableEntity
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val roles: Set<UserRole> = setOf(UserRole.USER),
    val userStatus: UserStatus = UserStatus.ACTIVE,
    
    // 조직 소속 정보 (본사ID 또는 매장ID)
    val organizationId: String? = null,
    val organizationType: String? = null, // HEADQUARTERS, STORE, SYSTEM

    // Security & Login tracking
    val lastLoginAt: LocalDateTime? = null,
    val failedLoginAttempts: Int = 0,
    val lockedUntil: LocalDateTime? = null,
    val emailVerifiedAt: LocalDateTime? = null,

    // Audit fields
    override val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: String? = null,
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val updatedBy: String? = null,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null,
    val version: Long = 0
) : AuditableEntity {

    private val _events = mutableListOf<UserEvent>()
    val events: List<UserEvent> get() = _events.toList()

    init {
        validateUser()
    }

    private fun validateUser() {
        require(username.isNotBlank()) { "사용자명은 필수입니다." }
        require(email.isNotBlank()) { "이메일은 필수입니다." }
        require(passwordHash.isNotBlank()) { "비밀번호는 필수입니다." }
        require(roles.isNotEmpty()) { "최소 하나의 역할이 필요합니다." }
    }

    fun isEmailVerified(): Boolean = emailVerifiedAt != null
    fun isLocked(): Boolean = lockedUntil?.isAfter(LocalDateTime.now()) == true
    fun hasRole(role: UserRole): Boolean = roles.contains(role)
    fun isSystemUser(): Boolean = organizationType == "SYSTEM"
    fun isHeadquartersUser(): Boolean = organizationType == "HEADQUARTERS"
    fun isStoreUser(): Boolean = organizationType == "STORE"

    fun login(loginAt: LocalDateTime = LocalDateTime.now()): User {
        val updatedUser = this.copy(
            lastLoginAt = loginAt,
            failedLoginAttempts = 0,
            updatedAt = loginAt
        )
        
        updatedUser._events.add(
            UserLoginEvent(
                userId = UserId.fromString(id),
                loginAt = loginAt
            )
        )
        
        return updatedUser
    }

    fun failLogin(): User {
        val updatedUser = this.copy(
            failedLoginAttempts = failedLoginAttempts + 1,
            updatedAt = LocalDateTime.now()
        )
        
        updatedUser._events.add(
            UserLoginFailedEvent(
                userId = UserId.fromString(id),
                failedAttempts = updatedUser.failedLoginAttempts
            )
        )
        
        return updatedUser
    }

    fun lock(until: LocalDateTime, reason: String = "Too many failed login attempts"): User {
        val lockedUser = this.copy(
            lockedUntil = until,
            updatedAt = LocalDateTime.now()
        )
        
        lockedUser._events.add(
            UserLockedEvent(
                userId = UserId.fromString(id),
                lockedUntil = until,
                reason = reason
            )
        )
        
        return lockedUser
    }

    fun unlock(updatedBy: String): User {
        val unlockedUser = this.copy(
            lockedUntil = null,
            failedLoginAttempts = 0,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        unlockedUser._events.add(
            UserUnlockedEvent(
                userId = UserId.fromString(id),
                unlockedBy = updatedBy
            )
        )
        
        return unlockedUser
    }

    fun changePassword(newPasswordHash: String, changedBy: String): User {
        val updatedUser = this.copy(
            passwordHash = newPasswordHash,
            updatedAt = LocalDateTime.now(),
            updatedBy = changedBy
        )
        
        updatedUser._events.add(
            UserPasswordChangedEvent(
                userId = UserId.fromString(id),
                changedBy = changedBy
            )
        )
        
        return updatedUser
    }

    fun changeStatus(newStatus: UserStatus, changedBy: String, reason: String? = null): User {
        val updatedUser = this.copy(
            userStatus = newStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = changedBy
        )
        
        updatedUser._events.add(
            UserStatusChangedEvent(
                userId = UserId.fromString(id),
                previousStatus = this.userStatus.name,
                newStatus = newStatus.name,
                reason = reason,
                changedBy = changedBy
            )
        )
        
        return updatedUser
    }

    fun assignRoles(newRoles: Set<UserRole>, updatedBy: String): User {
        val updatedUser = this.copy(
            roles = newRoles,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        updatedUser._events.add(
            UserRoleChangedEvent(
                userId = UserId.fromString(id),
                previousRoles = this.roles,
                newRoles = newRoles,
                changedBy = updatedBy
            )
        )
        
        return updatedUser
    }

    fun verifyEmail(verifiedAt: LocalDateTime = LocalDateTime.now()): User {
        val verifiedUser = this.copy(
            emailVerifiedAt = verifiedAt,
            updatedAt = verifiedAt
        )
        
        verifiedUser._events.add(
            UserEmailVerifiedEvent(
                userId = UserId.fromString(id),
                email = Email.fromString(email),
                verifiedAt = verifiedAt
            )
        )
        
        return verifiedUser
    }

    fun delete(deletedBy: String, reason: String? = null): User {
        val deletedUser = this.copy(
            isActive = false,
            userStatus = UserStatus.INACTIVE,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )
        
        deletedUser._events.add(
            UserDeletedEvent(
                userId = UserId.fromString(id),
                reason = reason,
                deletedBy = deletedBy
            )
        )
        
        return deletedUser
    }

    fun clearEvents() {
        _events.clear()
    }

    companion object {
        fun create(
            username: String,
            email: String,
            passwordHash: String,
            roles: Set<UserRole> = setOf(UserRole.USER),
            organizationId: String? = null,
            organizationType: String? = null,
            createdBy: String
        ): User {
            val user = User(
                username = username,
                email = email,
                passwordHash = passwordHash,
                roles = roles,
                organizationId = organizationId,
                organizationType = organizationType,
                createdBy = createdBy
            )
            
            user._events.add(
                UserCreatedEvent(
                    userId = UserId.fromString(user.id),
                    username = username,
                    email = Email.fromString(email),
                    roles = roles,
                    createdBy = createdBy
                )
            )
            
            return user
        }
    }
}
