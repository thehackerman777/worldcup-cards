package com.wcapp.backend.service

import com.wcapp.backend.dto.*
import com.wcapp.backend.entity.Role
import com.wcapp.backend.entity.User
import com.wcapp.backend.repository.UserRepository
import com.wcapp.backend.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("El nombre de usuario ya está en uso")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            displayName = request.displayName ?: request.username
        )

        val savedUser = userRepository.save(user)
        return generateAuthResponse(savedUser)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Credenciales inválidas")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Credenciales inválidas")
        }

        if (!user.enabled) {
            throw IllegalStateException("Cuenta deshabilitada")
        }

        return generateAuthResponse(user)
    }

    fun refreshToken(token: String): AuthResponse {
        if (!jwtUtil.isRefreshToken(token) || !jwtUtil.validateToken(token)) {
            throw IllegalArgumentException("Refresh token inválido")
        }

        val userId = jwtUtil.getUserIdFromToken(token)
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val userId = user.id.toString()
        val token = jwtUtil.generateToken(userId, user.username, user.role.name)
        val refreshToken = jwtUtil.generateRefreshToken(userId)

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
            expiresIn = jwtUtil.getExpirationMs(),
            user = UserResponse(
                id = userId,
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                role = user.role.name
            )
        )
    }
}
