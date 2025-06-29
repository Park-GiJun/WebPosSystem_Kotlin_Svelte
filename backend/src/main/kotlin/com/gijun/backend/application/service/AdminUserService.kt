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
        
        // 검색어가 있으면 Repository의 페이징 기능 사용
        if (!search.isNullOrBlank()) {
            val users = userRepository.findAllWithPaging(page, size, search)
            val totalCount = userRepository.countWithSearch(search)
            
            // 추가 필터링 (상태, 역할)
            var filteredUsers = users
            
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
            
            return AdminUserListResult(
                users = filteredUsers,
                totalCount = totalCount,
                page = page,
                size = size
            )
        }
        
        // 역할 필터가 있으면 역할별 조회 사용
        if (!role.isNullOrBlank() && role != "all") {
            val userRole = try {
                UserRole.valueOf(role.uppercase())
            } catch (e: Exception) {
                null
            }
            
            if (userRole != null) {
                val roleUsers = userRepository.findByRole(userRole.name)
                
                // 상태 필터 적용
                var filteredUsers = roleUsers
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
                
                // 페이징 적용
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
        }
        
        // 기본 페이징 조회
        val users = userRepository.findAllWithPaging(page, size, null)
        val totalCount = userRepository.countWithSearch(null)
        
        // 상태 필터만 적용
        var filteredUsers = users
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
        
        return AdminUserListResult(
            users = filteredUsers,
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
        
        // Optimistic Locking 재시도 로직
        var retryCount = 0
        val maxRetries = 3
        
        while (retryCount < maxRetries) {
            try {
                // 최신 사용자 정보를 다시 조회
                val existingUser = userRepository.findById(userId)
                    ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
                
                println("🔍 업데이트 시도 ${retryCount + 1}/$maxRetries:")
                println("  - 현재 버전: ${existingUser.version}")
                println("  - 현재 username: ${existingUser.username}")
                println("  - 새 username: $username")
                
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
                
                println("✅ 업데이트할 정보:")
                println("  - 새 버전: ${updatedUser.version}")
                println("  - 업데이트 시간: ${updatedUser.updatedAt}")
                
                // 저장 시도
                return userRepository.save(updatedUser)
                
            } catch (e: Exception) {
                println("❌ 업데이트 시도 실패 (${retryCount + 1}/$maxRetries):")
                println("  - 에러 타입: ${e.javaClass.simpleName}")
                println("  - 에러 메시지: ${e.message}")
                println("  - 전체 스택트레이스:")
                e.printStackTrace()
                
                // Version mismatch 에러인지 더 포괄적으로 확인
                val errorText = e.toString() + (e.message ?: "") + (e.cause?.message ?: "")
                val isVersionConflict = errorText.let { text ->
                    text.contains("Version does not match", ignoreCase = true) ||
                    text.contains("Optimistic", ignoreCase = true) ||
                    text.contains("version", ignoreCase = true) && text.contains("match", ignoreCase = true) ||
                    text.contains("Failed to update table", ignoreCase = true) && text.contains("version", ignoreCase = true)
                }
                
                if (isVersionConflict && retryCount < maxRetries - 1) {
                    println("⚠️ Optimistic Lock 충돌 발생, 재시도 중... (${retryCount + 1}/$maxRetries)")
                    retryCount++
                    
                    // 잠시 대기 후 재시도
                    kotlinx.coroutines.delay(100L * retryCount)
                    continue
                } else {
                    // 다른 에러이거나 최대 재시도 횟수 초과
                    println("❌ 재시도 불가능한 에러 또는 최대 재시도 횟수 초과")
                    throw e
                }
            }
        }
        
        throw IllegalStateException("사용자 업데이트에 실패했습니다. 동시 수정 충돌이 발생했습니다.")
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
