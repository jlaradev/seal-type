package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.UserRequest
import com.jlaradev.sealtype.dto.response.UserResponse
import com.jlaradev.sealtype.model.User
import com.jlaradev.sealtype.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class UserService(private val userRepository: UserRepository) {

    fun createUser(request: UserRequest): UserResponse {
        val usernameExists = userRepository.existsByUsername(request.username!!)
        if (usernameExists) {
            throw IllegalArgumentException("El nombre de usuario ya existe")
        }

        val user = User(
            username = request.username,
            passwordHash = request.passwordHash!!,
            displayName = request.displayName,
            avatarUrl = request.avatarUrl,
            isOnline = request.isOnline ?: false
        )

        val savedUser = userRepository.save(user)
        return toResponse(savedUser)
    }

    fun getUserById(id: UUID): UserResponse? {
        return userRepository.findById(id).orElse(null)?.let { toResponse(it) }
    }

    fun updateUser(id: UUID, request: UserRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        val updatedUser = User(
            id = user.id,
            username = user.username,
            passwordHash = user.passwordHash,
            displayName = request.displayName,
            avatarUrl = request.avatarUrl ?: user.avatarUrl,
            isOnline = request.isOnline ?: user.isOnline,
            lastSeen = user.lastSeen,
            createdAt = user.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)
        return toResponse(savedUser)
    }

    fun findByUsername(username: String): UserResponse? {
        return userRepository.findByUsername(username)?.let { toResponse(it) }
    }

    fun searchUsers(searchTerm: String): List<UserResponse> {
        return userRepository.findAllByUsernameContainingIgnoreCase(searchTerm)
            .map { toResponse(it) }
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

    @Transactional(readOnly = true)
    fun getUserStatus(userId: UUID): UserResponse? {
        return getUserById(userId)
    }

    private fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            username = user.username,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            isOnline = user.isOnline,
            lastSeen = user.lastSeen
        )
    }
}

