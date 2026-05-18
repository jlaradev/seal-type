package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.UserRequest
import com.jlaradev.sealtype.dto.response.UserResponse
import com.jlaradev.sealtype.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userService: UserService

    private val userId = UUID.randomUUID()
    private val lastSeen = LocalDateTime.now()

    @TestConfiguration
    class TestConfig {
        @Bean
        fun userService(): UserService = mockk(relaxed = false)
    }

    @BeforeEach
    fun setup() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun testCreateUserReturns201() {
        val request = UserRequest(
            username = "john_doe",
            passwordHash = "secure_pass_123",
            displayName = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg",
            isOnline = false
        )

        val expectedResponse = UserResponse(
            id = userId,
            username = "john_doe",
            displayName = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg",
            isOnline = false,
            lastSeen = lastSeen
        )

        every { userService.createUser(request) } returns expectedResponse

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id").isNotEmpty
            jsonPath("$.username") { value("john_doe") }
            jsonPath("$.displayName") { value("John Doe") }
            jsonPath("$.avatarUrl") { value("https://example.com/avatar.jpg") }
            jsonPath("$.isOnline") { value(false) }
            jsonPath("$.lastSeen").isNotEmpty
            jsonPath("$.passwordHash").doesNotExist()
            jsonPath("$.createdAt").doesNotExist()
        }
    }

    @Test
    fun testGetUserByIdReturns200() {
        val expectedResponse = UserResponse(
            id = userId,
            username = "john_doe",
            displayName = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg",
            isOnline = true,
            lastSeen = lastSeen
        )

        every { userService.getUserById(userId) } returns expectedResponse

        mockMvc.get("/api/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(userId.toString()) }
            jsonPath("$.username") { value("john_doe") }
            jsonPath("$.displayName") { value("John Doe") }
            jsonPath("$.avatarUrl") { value("https://example.com/avatar.jpg") }
            jsonPath("$.isOnline") { value(true) }
            jsonPath("$.lastSeen").isNotEmpty
            jsonPath("$.passwordHash").doesNotExist()
        }
    }

    @Test
    fun testUpdateUserReturns200() {
        val request = UserRequest(
            username = "john_doe",
            passwordHash = "secure_pass_123",
            displayName = "John Updated",
            avatarUrl = "https://example.com/new-avatar.jpg"
        )

        val expectedResponse = UserResponse(
            id = userId,
            username = "john_doe",
            displayName = "John Updated",
            avatarUrl = "https://example.com/new-avatar.jpg",
            isOnline = false,
            lastSeen = lastSeen
        )

        every { userService.updateUser(userId, request) } returns expectedResponse

        mockMvc.put("/api/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("John Updated") }
            jsonPath("$.avatarUrl") { value("https://example.com/new-avatar.jpg") }
        }
    }

    @Test
    fun testSearchUsersReturns200() {
        val user1 = UserResponse(
            id = userId,
            username = "john_doe",
            displayName = "John Doe",
            isOnline = true,
            lastSeen = lastSeen
        )
        val user2 = UserResponse(
            id = UUID.randomUUID(),
            username = "john_smith",
            displayName = "John Smith",
            isOnline = false,
            lastSeen = lastSeen
        )

        every { userService.searchUsers("john") } returns listOf(user1, user2)

        mockMvc.get("/api/users/search?term=john") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$").isArray()
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].id").isNotEmpty
            jsonPath("$[0].username") { value("john_doe") }
            jsonPath("$[0].displayName") { value("John Doe") }
            jsonPath("$[0].isOnline") { value(true) }
            jsonPath("$[1].username") { value("john_smith") }
            jsonPath("$[1].isOnline") { value(false) }
        }
    }

    @Test
    fun testGetUserByIdNotFound() {
        every { userService.getUserById(userId) } returns null

        mockMvc.get("/api/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun testValidationErrorReturns400() {
        val invalidRequest = UserRequest(
            username = "",
            passwordHash = "123",
            displayName = ""
        )

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Validation Failed") }
            jsonPath("$.message") { value("Errores de validación en los datos enviados") }
            jsonPath("$.details").isNotEmpty
            jsonPath("$.details.username").isNotEmpty
            jsonPath("$.details.displayName").isNotEmpty
            jsonPath("$.timestamp").isNotEmpty
            jsonPath("$.path").isNotEmpty
        }
    }

    @Test
    fun testDuplicateUsernameReturns400() {
        val request = UserRequest(
            username = "existing_user",
            passwordHash = "secure_pass_123",
            displayName = "Existing User"
        )

        every { userService.createUser(request) } throws IllegalArgumentException("El nombre de usuario ya existe")

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
            jsonPath("$.message") { value("El nombre de usuario ya existe") }
            jsonPath("$.path") { value("/api/users") }
            jsonPath("$.timestamp").isNotEmpty
        }
    }

    @Test
    fun testUpdateUserNotFoundReturns400() {
        val request = UserRequest(
            username = "john_doe",
            passwordHash = "secure_pass_123",
            displayName = "New Name"
        )

        every { userService.updateUser(userId, request) } throws IllegalArgumentException("Usuario no encontrado")

        mockMvc.put("/api/users/$userId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
            jsonPath("$.path") { value("/api/users/$userId") }
        }
    }

    @Test
    fun testSearchUsersEmptyReturns200() {
        every { userService.searchUsers("xyz_no_existe") } returns emptyList()

        mockMvc.get("/api/users/search?term=xyz_no_existe") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$").isArray()
            jsonPath("$.length()") { value(0) }
        }
    }
}