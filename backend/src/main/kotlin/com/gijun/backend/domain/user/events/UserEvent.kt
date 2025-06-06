package com.gijun.backend.domain.user.events

import com.gijun.backend.domain.user.vo.UserId
import com.gijun.backend.domain.user.vo.Email
import com.gijun.backend.domain.user.enums.UserRole
import java.time.LocalDateTime

sealed class UserEvent(
    open val userId: UserId,
    open val occurredAt: LocalDateTime = LocalDateTime.now()
)

data class UserCreatedEvent(
    override val userId: UserId,
    val username: String,
    val email: Email,
    val roles: Set<UserRole>,
    val createdBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : UserEvent(userId, occurredAt)

data class UserRoleChangedEvent(
    override val userId: UserId,
    val previousRoles: Set<UserRole>,
    val newRoles: Set<UserRole>,
    val changedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : UserEvent(userId, occurredAt)

data class UserStatusChangedEvent(
    override val userId: UserId,
    val previousStatus: String,
    val newStatus: String,
    val reason: String? = null,
    val changedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : UserEvent(userId, occurredAt)

data class UserDeletedEvent(
    override val userId: UserId,
    val reason: String? = null,
    val deletedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : UserEvent(userId, occurredAt)

data class UserPasswordChangedEvent(
    override val userId: UserId,
    val changedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : UserEvent(userId, occurredAt)