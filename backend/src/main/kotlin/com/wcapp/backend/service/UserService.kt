package com.wcapp.backend.service

import com.wcapp.backend.dto.UpdateUserRequest
import com.wcapp.backend.dto.UserResponse
import com.wcapp.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getUserById(userId: String): UserResponse {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        return UserResponse(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            role = user.role.name
        )
    }

    @Transactional
    fun updateUser(userId: String, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        request.displayName?.let { user.displayName = it }
        request.avatarUrl?.let { user.avatarUrl = it }
        request.phone?.let { user.phone = it }

        val updated = userRepository.save(user)

        return UserResponse(
            id = updated.id.toString(),
            username = updated.username,
            email = updated.email,
            displayName = updated.displayName,
            avatarUrl = updated.avatarUrl,
            role = updated.role.name
        )
    }
}
