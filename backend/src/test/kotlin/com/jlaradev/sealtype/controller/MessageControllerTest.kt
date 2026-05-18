package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.MessageRequest
import com.jlaradev.sealtype.dto.response.MessageResponse
import com.jlaradev.sealtype.enums.MessageType
import com.jlaradev.sealtype.service.MessageService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

@WebMvcTest(MessageController::class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var messageService: MessageService

    private val messageId = UUID.randomUUID()
    private val chatId = UUID.randomUUID()
    private val senderId = UUID.randomUUID()
    private val now = LocalDateTime.now()

    @TestConfiguration
    class TestConfig {
        @Bean
        fun messageService(): MessageService = mockk(relaxed = false)
    }

    @BeforeEach
    fun setup() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun testSendMessageReturns201() {
        val request = MessageRequest(
            content = "Test message",
            chatId = chatId,
            type = MessageType.TEXT
        )

        val expectedResponse = MessageResponse(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = "Test message",
            type = MessageType.TEXT,
            isDeleted = false,
            attachmentId = null,
            createdAt = now,
            updatedAt = now
        )

        every { messageService.sendMessage(any(), senderId) } returns expectedResponse

        mockMvc.post("/api/chats/$chatId/messages") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", senderId.toString())
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id").isNotEmpty
            jsonPath("$.chatId") { value(chatId.toString()) }
            jsonPath("$.senderId") { value(senderId.toString()) }
            jsonPath("$.content") { value("Test message") }
            jsonPath("$.type") { value("TEXT") }
            jsonPath("$.isDeleted") { value(false) }
            jsonPath("$.createdAt").isNotEmpty
        }
    }

    @Test
    fun testGetMessageByIdReturns200() {
        val expectedResponse = MessageResponse(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = "Test message",
            type = MessageType.TEXT,
            isDeleted = false,
            attachmentId = null,
            createdAt = now,
            updatedAt = now
        )

        every { messageService.getMessageById(messageId) } returns expectedResponse

        mockMvc.get("/api/messages/$messageId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(messageId.toString()) }
            jsonPath("$.content") { value("Test message") }
            jsonPath("$.type") { value("TEXT") }
        }
    }

    @Test
    fun testUpdateMessageReturns200() {
        val request = MessageRequest(
            content = "Updated message",
            chatId = chatId,
            type = MessageType.TEXT
        )

        val expectedResponse = MessageResponse(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = "Updated message",
            type = MessageType.TEXT,
            isDeleted = false,
            attachmentId = null,
            createdAt = now,
            updatedAt = now
        )

        every { messageService.updateMessage(messageId, request, senderId) } returns expectedResponse

        mockMvc.put("/api/messages/$messageId") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", senderId.toString())
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { value("Updated message") }
        }
    }

    @Test
    fun testDeleteMessageReturns204() {
        io.mockk.justRun { messageService.deleteMessage(messageId, senderId) }

        mockMvc.delete("/api/messages/$messageId") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", senderId.toString())
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun testGetMessagesByChatReturns200() {
        val message1 = MessageResponse(
            id = UUID.randomUUID(),
            chatId = chatId,
            senderId = senderId,
            content = "Message 1",
            type = MessageType.TEXT,
            isDeleted = false,
            attachmentId = null,
            createdAt = now,
            updatedAt = now
        )
        val message2 = MessageResponse(
            id = UUID.randomUUID(),
            chatId = chatId,
            senderId = senderId,
            content = "Message 2",
            type = MessageType.IMAGE,
            isDeleted = false,
            attachmentId = null,
            createdAt = now,
            updatedAt = now
        )

        val pageRequest = PageRequest.of(0, 20, Sort.by("createdAt").descending())
        val page: Page<MessageResponse> = PageImpl(listOf(message1, message2), pageRequest, 2)

        every { messageService.getMessagesByChat(chatId, any()) } returns page

        mockMvc.get("/api/chats/$chatId/messages") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content").isArray()
            jsonPath("$.content.length()") { value(2) }
            jsonPath("$.content[0].content") { value("Message 1") }
            jsonPath("$.content[1].content") { value("Message 2") }
            jsonPath("$.totalElements") { value(2) }
        }
    }

    @Test
    fun testGetLastMessageReturns200() {
        val expectedResponse = MessageResponse(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            content = "Last message",
            type = MessageType.TEXT,
            isDeleted = false,
            attachmentId = null,
            createdAt = now,
            updatedAt = now
        )

        every { messageService.getLastMessage(chatId) } returns expectedResponse

        mockMvc.get("/api/chats/$chatId/messages/last") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { value("Last message") }
        }
    }

    @Test
    fun testCountMessagesAfterDateReturns200() {
        every { messageService.countMessagesAfterDate(chatId, any()) } returns 5L

        val afterDate = LocalDateTime.now().minusHours(1)

        mockMvc.get("/api/chats/$chatId/messages/count?after=$afterDate") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.count") { value(5) }
        }
    }

    @Test
    fun testGetMessageByIdNotFound() {
        every { messageService.getMessageById(messageId) } returns null

        mockMvc.get("/api/messages/$messageId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun testSendMessageValidationErrorReturns400() {
        val invalidRequest = MessageRequest(
            content = "",
            chatId = chatId
        )

        mockMvc.post("/api/chats/$chatId/messages") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", senderId.toString())
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Validation Failed") }
        }
    }

    @Test
    fun testUpdateMessageNotFoundReturns400() {
        val request = MessageRequest(
            content = "Update",
            chatId = chatId
        )

        every { messageService.updateMessage(messageId, request, senderId) } throws IllegalArgumentException("Mensaje no encontrado")

        mockMvc.put("/api/messages/$messageId") {
            contentType = MediaType.APPLICATION_JSON
            header("X-User-Id", senderId.toString())
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }
}






