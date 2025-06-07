package com.gijun.backend.application.service

import com.gijun.backend.application.port.out.UserRepository
import com.gijun.backend.domain.user.entities.User
import com.gijun.backend.domain.user.enums.UserRole
import com.gijun.backend.domain.user.enums.UserStatus
import kotlinx.coroutines.flow.toList
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun getAllUsers(
        page: Int = 0,
        size: Int = 20,
        status: String? = null,
        role: String? = null,
        search: String? = null
    ): AdminUserListResult {
        
        // 모든 사용자 조회
        val allUsers = userRepository.findAll()
        
        // 필터링
        var filteredUsers = allUsers
        
        // 상태 필터
        if (!status.isNullOrBlank() && status != "all") {
            val userStatus = try {
                UserStatus.valueOf(status.uppercase())
            } catch (e: Exception) {
                null
            }
            if (userStatus != null) {
                filteredUsers = filteredUsers.filter { it.userStatus == userStatus }
            }
        }
        
        // 역할 필터
        if (!role.isNullOrBlank() && role != "all") {
            val userRole = try {
                UserRole.valueOf(role.uppercase())
            } catch (e: Exception) {
                null
            }
            if (userRole != null) {
                filteredUsers = filteredUsers.filter { it.roles.contains(userRole) }
            }
        }
        
        // 검색 필터
        if (!search.isNullOrBlank()) {
            filteredUsers = filteredUsers.filter { user ->
                user.username.contains(search, ignoreCase = true) ||
                user.email.contains(search, ignoreCase = true)
            }
        }
        
        // 페이징
        val totalCount = filteredUsers.size.toLong()
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, filteredUsers.size)
        
        val pagedUsers = if (startIndex < filteredUsers.size) {
            filteredUsers.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return AdminUserListResult(
            users = pagedUsers,
            totalCount = totalCount,
            page = page,
            size = size
        )
    }

    suspend fun createUser(
        username: String,
        email: String,
        password: String,
        roles: List<String>,
        createdBy: String
    ): User {
        
        // 중복 체크
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("이미 존재하는 사용자명입니다.")
        }
        
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }
        
        // 역할 변환 및 검증
        val userRoles = roles.mapNotNull { roleStr ->
            try {
                UserRole.valueOf(roleStr.uppercase())
            } catch (e: Exception) {
                null
            }
        }.toSet()
        
        if (userRoles.isEmpty()) {
            throw IllegalArgumentException("유효한 역할이 최소 하나는 필요합니다.")
        }
        
        // 슈퍼 관리자 역할 검증
        if (userRoles.contains(UserRole.SUPER_ADMIN) && userRoles.size > 1) {
            throw IllegalArgumentException("슈퍼 관리자는 단독으로만 설정할 수 있습니다.")
        }
        
        // 비밀번호 해시화
        val hashedPassword = passwordEncoder.encode(password)
        
        // 사용자 생성
        val newUser = User(
            username = username,
            email = email,
            passwordHash = hashedPassword,
            roles = userRoles,
            userStatus = UserStatus.PENDING_VERIFICATION,
            createdBy = createdBy,
            updatedBy = createdBy
        )
        
        return userRepository.save(newUser)
    }

    suspend fun updateUser(
        userId: String,
        username: String,
        email: String,
        roles: List<String>,
        userStatus: String?,
        updatedBy: String
    ): User {
        
        val existingUser = userRepository.findById(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        // 중복 체크 (본인 제외)
        val userWithSameUsername = userRepository.findByUsername(username)
        if (userWithSameUsername != null && userWithSameUsername.id != userId) {
            throw IllegalArgumentException("이미 존재하는 사용자명입니다.")
        }
        
        val userWithSameEmail = userRepository.findByEmail(email)
        if (userWithSameEmail != null && userWithSameEmail.id != userId) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }
        
        // 역할 변환 및 검증
        val userRoles = roles.mapNotNull { roleStr ->
            try {
                UserRole.valueOf(roleStr.uppercase())
            } catch (e: Exception) {
                null
            }
        }.toSet()
        
        if (userRoles.isEmpty()) {
            throw IllegalArgumentException("유효한 역할이 최소 하나는 필요합니다.")
        }
        
        // 슈퍼 관리자 역할 검증
        if (userRoles.contains(UserRole.SUPER_ADMIN) && userRoles.size > 1) {
            throw IllegalArgumentException("슈퍼 관리자는 단독으로만 설정할 수 있습니다.")
        }
        
        // 상태 변환
        val newUserStatus = if (!userStatus.isNullOrBlank()) {
            try {
                UserStatus.valueOf(userStatus.uppercase())
            } catch (e: Exception) {
                existingUser.userStatus
            }
        } else {
            existingUser.userStatus
        }
        
        // 사용자 업데이트
        val updatedUser = existingUser.copy(
            username = username,
            email = email,
            roles = userRoles,
            userStatus = newUserStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = existingUser.version + 1
        )
        
        return userRepository.save(updatedUser)
    }

    suspend fun deleteUser(userId: String, deletedBy: String) {
        val existingUser = userRepository.findById(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        // 슈퍼 관리자는 삭제할 수 없음
        if (existingUser.hasRole(UserRole.SUPER_ADMIN)) {
            throw IllegalArgumentException("슈퍼 관리자는 삭제할 수 없습니다.")
        }
        
        val deletedUser = existingUser.delete(deletedBy)
        userRepository.save(deletedUser)
    }

    suspend fun assignRoles(
        userId: String,
        roles: List<String>,
        updatedBy: String
    ): User {
        
        val existingUser = userRepository.findById(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        // 역할 변환 및 검증
        val userRoles = roles.mapNotNull { roleStr ->
            try {
                UserRole.valueOf(roleStr.uppercase())
            } catch (e: Exception) {
                null
            }
        }.toSet()
        
        if (userRoles.isEmpty()) {
            throw IllegalArgumentException("유효한 역할이 최소 하나는 필요합니다.")
        }
        
        // 슈퍼 관리자 역할 검증
        if (userRoles.contains(UserRole.SUPER_ADMIN) && userRoles.size > 1) {
            throw IllegalArgumentException("슈퍼 관리자는 단독으로만 설정할 수 있습니다.")
        }
        
        val updatedUser = existingUser.copy(
            roles = userRoles,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy,
            version = existingUser.version + 1
        )
        
        return userRepository.save(updatedUser)
    }

    suspend fun unlockUser(userId: String, updatedBy: String): User {
        val existingUser = userRepository.findById(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        val unlockedUser = existingUser.unlock(updatedBy)
        return userRepository.save(unlockedUser)
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.findById(userId)
    }
}

data class AdminUserListResult(
    val users: List<User>,
    val totalCount: Long,
    val page: Int,
    val size: Int
)
