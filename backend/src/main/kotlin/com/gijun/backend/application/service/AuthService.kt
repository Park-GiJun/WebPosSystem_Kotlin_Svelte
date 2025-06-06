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
import java.time.LocalDateTime

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

        // 첫 번째 사용자인지 확인
        val userCount = userRepository.count()
        val isFirstUser = userCount == 0L

        val user = createUser(command, isFirstUser)
        val savedUser = userRepository.save(user)

        // 첫 번째 사용자가 아닌 경우에만 created_by를 업데이트
        val finalUser = if (!isFirstUser && savedUser.createdBy == null) {
            val updatedUser = savedUser.copy(
                createdBy = savedUser.id, // 자기 자신을 생성자로 설정
                updatedBy = savedUser.id,
                version = savedUser.version + 1
            )
            userRepository.save(updatedUser)
        } else {
            savedUser
        }

        logger.info("User registered successfully: ${finalUser.id}")
        return finalUser
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
        
        if (!loginSuccess) {
            // 비밀번호 실패 시에만 실패 횟수 업데이트 (try-catch로 감싸서 충돌 무시)
            try {
                val updatedUser = user.recordLoginAttempt(false, command.username)
                userRepository.save(updatedUser)
            } catch (e: Exception) {
                logger.warn("Failed to record failed login attempt for user: ${command.username}, but continuing with login failure", e)
            }
            throw AuthenticationException("Invalid username or password")
        }

        // 로그인 성공 - 별도 처리로 실패해도 로그인은 계속 진행
        val finalUser = try {
            // 성공 시에는 실패 횟수만 초기화하고 버전은 증가시키지 않음
            val updatedUser = user.copy(
                lastLoginAt = LocalDateTime.now(),
                failedLoginAttempts = 0,
                updatedAt = LocalDateTime.now(),
                updatedBy = command.username
                // version 업데이트 제거
            )
            userRepository.save(updatedUser)
        } catch (e: Exception) {
            logger.warn("Failed to record successful login for user: ${command.username}, proceeding with login", e)
            user // 실패해도 로그인은 계속 진행
        }

        val authorities = user.roles.map { "ROLE_${it.name}" }
        val token = jwtUtil.generateToken(user.username, authorities)

        logger.info("User logged in successfully: ${user.id}")
        return AuthResult(
            user = finalUser,
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

    private suspend fun createUser(command: RegisterCommand, isFirstUser: Boolean = false): User {
        val passwordHash = passwordEncoder.encode(command.password)

        return User(
            username = command.username,
            email = command.email,
            passwordHash = passwordHash,
            roles = if (isFirstUser) setOf(UserRole.SUPER_ADMIN) else setOf(UserRole.USER),
            userStatus = if (isFirstUser) UserStatus.ACTIVE else UserStatus.PENDING_VERIFICATION,
            createdBy = null // 일단 null로 설정하고 나중에 업데이트
        )
    }
}

// Exception classes
class AuthenticationException(message: String) : RuntimeException(message)
class UserAlreadyExistsException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)