package com.gijun.backend.application.service

import com.gijun.backend.application.port.`in`.AuthResult
import com.gijun.backend.application.port.`in`.AuthUseCase
import com.gijun.backend.application.port.`in`.LoginCommand
import com.gijun.backend.application.port.`in`.RegisterCommand
import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.configuration.JwtUtil
import com.gijun.backend.domain.user.entity.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) : AuthUseCase {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    override suspend fun register(command: RegisterCommand): User {
        logger.info("Registering new user: ${command.username}")

        validateUserDoesNotExist(command)

        val user = createUser(command)
        val savedUser = userRepository.save(user)

        logger.info("User registered successfully: ${savedUser.id}")
        return savedUser
    }

    override suspend fun login(command: LoginCommand): AuthResult {
        logger.info("User login attempt: ${command.username}")

        val user = userRepository.findByUsername(command.username)
            ?: throw AuthenticationException("Invalid username or password")

        if (!user.canLogin()) {
            val reason = when {
                !user.isActive -> "Account is deactivated"
                user.isLocked() -> "Account is locked until ${user.lockedUntil}"
                user.userStatus == UserStatus.SUSPENDED -> "Account is suspended"
                user.userStatus == UserStatus.PENDING_VERIFICATION -> "Please verify your email first"
                else -> "Account is not available for login"
            }
            throw AuthenticationException(reason)
        }

        val loginSuccess = passwordEncoder.matches(command.password, user.passwordHash)
        val updatedUser = user.recordLoginAttempt(loginSuccess, command.username)
        userRepository.save(updatedUser)

        if (!loginSuccess) {
            throw AuthenticationException("Invalid username or password")
        }

        val authorities = user.roles.map { "ROLE_${it.name}" }
        val token = jwtUtil.generateToken(user.username, authorities)

        logger.info("User logged in successfully: ${user.id}")
        return AuthResult(
            user = updatedUser,
            token = token,
            expiresIn = 86400000L // 24시간
        )
    }

    override suspend fun validateToken(token: String): User? {
        return try {
            if (!jwtUtil.validateToken(token)) {
                return null
            }

            val username = jwtUtil.getUsernameFromToken(token)
            userRepository.findByUsername(username)?.takeIf { it.canLogin() }
        } catch (e: Exception) {
            logger.debug("Token validation failed", e)
            null
        }
    }

    // New methods for V2 features
    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String, updatedBy: String): User {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("User not found")

        if (!passwordEncoder.matches(currentPassword, user.passwordHash)) {
            throw AuthenticationException("Current password is incorrect")
        }

        val newPasswordHash = passwordEncoder.encode(newPassword)
        val updatedUser = user.copy(
            passwordHash = newPasswordHash,
            updatedAt = java.time.LocalDateTime.now(),
            updatedBy = updatedBy,
            version = user.version + 1
        )

        return userRepository.save(updatedUser)
    }

    suspend fun verifyEmail(userId: String): User {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("User not found")

        val verifiedUser = user.verifyEmail(userId)
        return userRepository.save(verifiedUser)
    }

    suspend fun unlockUser(userId: String, updatedBy: String): User {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("User not found")

        val unlockedUser = user.unlock(updatedBy)
        return userRepository.save(unlockedUser)
    }

    private suspend fun validateUserDoesNotExist(command: RegisterCommand) {
        if (userRepository.existsByUsername(command.username)) {
            throw UserAlreadyExistsException("Username '${command.username}' already exists")
        }

        if (userRepository.existsByEmail(command.email)) {
            throw UserAlreadyExistsException("Email '${command.email}' already exists")
        }
    }

    private fun createUser(command: RegisterCommand): User {
        val passwordHash = passwordEncoder.encode(command.password)

        return User(
            username = command.username,
            email = command.email,
            passwordHash = passwordHash,
            roles = setOf(UserRole.USER),
            userStatus = UserStatus.PENDING_VERIFICATION, // 이메일 인증 필요
            createdBy = command.username // 자기 자신이 생성
        )
    }
}

// Exception classes
class AuthenticationException(message: String) : RuntimeException(message)
class UserAlreadyExistsException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)