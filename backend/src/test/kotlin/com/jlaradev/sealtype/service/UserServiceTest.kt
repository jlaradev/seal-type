package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.UserRequest
import com.jlaradev.sealtype.model.User
import com.jlaradev.sealtype.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    private lateinit var testUser: User
    private val testUserId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        // Crear mocks
        userRepository = mockk()
        userService = UserService(userRepository)

        // Crear usuario de prueba
        testUser = User(
            id = testUserId,
            username = "johndoe",
            passwordHash = "hashedPassword123",
            displayName = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg",
            isOnline = true,
            lastSeen = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun testCreateUserSuccessfully() {
        // Arrange
        val request = UserRequest(
            username = "johndoe",
            passwordHash = "securePassword123",
            displayName = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        every { userRepository.existsByUsername("johndoe") } returns false
        every { userRepository.save(any()) } returns testUser

        // Act
        val response = userService.createUser(request)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testUserId)
            .hasFieldOrPropertyWithValue("username", "johndoe")

        verify(exactly = 1) { userRepository.existsByUsername("johndoe") }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun testCreateUserWithDuplicateUsername() {
        // Arrange
        val request = UserRequest(
            username = "johndoe",
            passwordHash = "securePassword123",
            displayName = "John Doe"
        )

        every { userRepository.existsByUsername("johndoe") } returns true

        // Act & Assert
        assertThatThrownBy { userService.createUser(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("El nombre de usuario ya existe")

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun testGetUserByIdFound() {
        // Arrange
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)

        // Act
        val response = userService.getUserById(testUserId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testUserId)
            .hasFieldOrPropertyWithValue("username", "johndoe")

        verify(exactly = 1) { userRepository.findById(testUserId) }
    }

    @Test
    fun testGetUserByIdNotFound() {
        // Arrange
        every { userRepository.findById(testUserId) } returns Optional.empty()

        // Act
        val response = userService.getUserById(testUserId)

        // Assert
        assertThat(response).isNull()
    }

    @Test
    fun testUpdateUserSuccessfully() {
        // Arrange
        val updateRequest = UserRequest(
            displayName = "John Updated",
            avatarUrl = "https://example.com/new-avatar.jpg",
            isOnline = false
        )

        val updatedUser = User(
            id = testUserId,
            username = "johndoe",
            passwordHash = "hashedPassword123",
            displayName = "John Updated",
            avatarUrl = "https://example.com/new-avatar.jpg",
            isOnline = false,
            lastSeen = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { userRepository.save(any()) } returns updatedUser

        // Act
        val response = userService.updateUser(testUserId, updateRequest)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("displayName", "John Updated")

        verify(exactly = 1) { userRepository.findById(testUserId) }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun testUpdateUserNotFound() {
        // Arrange
        val updateRequest = UserRequest(
            displayName = "John Updated"
        )

        every { userRepository.findById(testUserId) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy { userService.updateUser(testUserId, updateRequest) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Usuario no encontrado")

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun testFindByUsernameFound() {
        // Arrange
        every { userRepository.findByUsername("johndoe") } returns testUser

        // Act
        val response = userService.findByUsername("johndoe")

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("username", "johndoe")
    }

    @Test
    fun testFindByUsernameNotFound() {
        // Arrange
        every { userRepository.findByUsername("notexist") } returns null

        // Act
        val response = userService.findByUsername("notexist")

        // Assert
        assertThat(response).isNull()
    }

    @Test
    fun testSearchUsers() {
        // Arrange
        val user2 = User(
            id = UUID.randomUUID(),
            username = "johnsmith",
            passwordHash = "hashedPassword456",
            displayName = "John Smith",
            isOnline = false,
            lastSeen = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { userRepository.findAllByUsernameContainingIgnoreCase("john") } returns listOf(testUser, user2)

        // Act
        val results = userService.searchUsers("john")

        // Assert
        assertThat(results)
            .hasSize(2)
            .extracting("username")
            .containsExactlyInAnyOrder("johndoe", "johnsmith")

        verify(exactly = 1) { userRepository.findAllByUsernameContainingIgnoreCase("john") }
    }

    @Test
    fun testExistsByUsernameTrue() {
        // Arrange
        every { userRepository.existsByUsername("johndoe") } returns true

        // Act
        val exists = userService.existsByUsername("johndoe")

        // Assert
        assertThat(exists).isTrue()
    }

    @Test
    fun testExistsByUsernameFalse() {
        // Arrange
        every { userRepository.existsByUsername("notexist") } returns false

        // Act
        val exists = userService.existsByUsername("notexist")

        // Assert
        assertThat(exists).isFalse()
    }

    @Test
    fun testGetUserStatus() {
        // Arrange
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)

        // Act
        val response = userService.getUserStatus(testUserId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("isOnline", true)
    }
}


