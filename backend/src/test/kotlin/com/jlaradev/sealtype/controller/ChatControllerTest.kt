package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.ChatRequest
import com.jlaradev.sealtype.dto.response.ChatResponse
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.service.ChatService
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@WebMvcTest(ChatController::class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var chatService: ChatService

    private val chatId = UUID.randomUUID()
    private val creatorId = UUID.randomUUID()
    private val now = LocalDateTime.now()

    @TestConfiguration
    class TestConfig {
        @Bean
        fun chatService(): ChatService = mockk(relaxed = false)
    }

    @BeforeEach
    fun setup() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun testCreateChatReturns201() {
        val request = ChatRequest(
            type = ChatType.GROUP,
            title = "Test Chat",
            groupIcon = "icon_url",
            memberIds = listOf(UUID.randomUUID())
        )

        val expectedResponse = ChatResponse(
            id = chatId,
            type = ChatType.GROUP,
            title = "Test Chat",
            groupIcon = "icon_url",
            isDeleted = false,
            createdById = creatorId
        )

        every { chatService.createChat(request, creatorId) } returns expectedResponse

        mockMvc.post("/api/chats") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", creatorId.toString())
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id").isNotEmpty
            jsonPath("$.type") { value("GROUP") }
            jsonPath("$.title") { value("Test Chat") }
            jsonPath("$.groupIcon") { value("icon_url") }
            jsonPath("$.createdById") { value(creatorId.toString()) }
            jsonPath("$.isDeleted") { value(false) }
        }
    }

    @Test
    fun testGetChatByIdReturns200() {
        val expectedResponse = ChatResponse(
            id = chatId,
            type = ChatType.GROUP,
            title = "Test Chat",
            groupIcon = "icon_url",
            isDeleted = false,
            createdById = creatorId
        )

        every { chatService.getChatById(chatId) } returns expectedResponse

        mockMvc.get("/api/chats/$chatId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(chatId.toString()) }
            jsonPath("$.type") { value("GROUP") }
            jsonPath("$.title") { value("Test Chat") }
            jsonPath("$.groupIcon") { value("icon_url") }
            jsonPath("$.isDeleted") { value(false) }
        }
    }

    @Test
    fun testUpdateChatReturns200() {
        val request = ChatRequest(
            type = ChatType.GROUP,
            title = "Updated Chat",
            groupIcon = "new_icon"
        )

        val expectedResponse = ChatResponse(
            id = chatId,
            type = ChatType.GROUP,
            title = "Updated Chat",
            groupIcon = "new_icon",
            isDeleted = false,
            createdById = creatorId
        )

        every { chatService.updateChat(chatId, request) } returns expectedResponse

        mockMvc.put("/api/chats/$chatId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Updated Chat") }
            jsonPath("$.groupIcon") { value("new_icon") }
        }
    }

    @Test
    fun testDeleteChatReturns204() {
        io.mockk.justRun { chatService.deleteChat(chatId) }

        mockMvc.delete("/api/chats/$chatId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun testSearchChatsReturns200() {
        val chat1 = ChatResponse(
            id = chatId,
            type = ChatType.GROUP,
            title = "Test Chat",
            groupIcon = "icon_url",
            isDeleted = false,
            createdById = creatorId
        )
        val chat2 = ChatResponse(
            id = UUID.randomUUID(),
            type = ChatType.DIRECT,
            title = null,
            groupIcon = null,
            isDeleted = false,
            createdById = creatorId
        )

        every { chatService.searchChats("test") } returns listOf(chat1, chat2)

        mockMvc.get("/api/chats/search?term=test") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$").isArray()
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Test Chat") }
            jsonPath("$[1].type") { value("DIRECT") }
        }
    }

    @Test
    fun testGetChatByIdNotFound() {
        every { chatService.getChatById(chatId) } returns null

        mockMvc.get("/api/chats/$chatId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun testCreateChatValidationErrorReturns400() {
        val request = ChatRequest(
            type = ChatType.GROUP,
            title = null
        )

        every { chatService.createChat(request, creatorId) } throws IllegalArgumentException("Los chats grupales deben tener un título")

        mockMvc.post("/api/chats") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", creatorId.toString())
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
        }
    }

    @Test
    fun testUpdateChatNotFoundReturns400() {
        val request = ChatRequest(
            type = ChatType.GROUP,
            title = "Update"
        )

        every { chatService.updateChat(chatId, request) } throws IllegalArgumentException("Chat no encontrado")

        mockMvc.put("/api/chats/$chatId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
        }
    }
}





