package com.gijun.backend.application.port.`in`

import com.gijun.backend.domain.user.entities.User

interface AuthUseCase {

    suspend fun register(command: RegisterCommand): User

    suspend fun login(command: LoginCommand): AuthResult

    suspend fun validateToken(token: String): User?
}

data class RegisterCommand(
    val username: String,
    val email: String,
    val password: String
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length in 3..50) { "Username must be between 3 and 50 characters" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))) { "Invalid email format" }
    }
}

data class LoginCommand(
    val username: String,
    val password: String
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(password.isNotBlank()) { "Password cannot be blank" }
    }
}

data class AuthResult(
    val user: User,
    val token: String,
    val expiresIn: Long
)
