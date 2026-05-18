package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.MessageAttachmentRequest
import com.jlaradev.sealtype.dto.response.MessageAttachmentResponse
import com.jlaradev.sealtype.service.MessageAttachmentService
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
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@WebMvcTest(MessageAttachmentController::class)
@AutoConfigureMockMvc(addFilters = false)
class MessageAttachmentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var messageAttachmentService: MessageAttachmentService

    private val attachmentId = UUID.randomUUID()
    private val messageId = UUID.randomUUID()
    private val now = LocalDateTime.now()

    @TestConfiguration
    class TestConfig {
        @Bean
        fun messageAttachmentService(): MessageAttachmentService = mockk(relaxed = false)
    }

    @BeforeEach
    fun setup() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun testCreateAttachmentReturns201() {
        val request = MessageAttachmentRequest(
            messageId = messageId,
            fileUrl = "https://example.com/file.pdf",
            fileName = "document.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf"
        )

        val expectedResponse = MessageAttachmentResponse(
            id = attachmentId,
            messageId = messageId,
            fileUrl = "https://example.com/file.pdf",
            fileName = "document.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf"
        )

        every { messageAttachmentService.createAttachment(any()) } returns expectedResponse

        mockMvc.post("/api/messages/$messageId/attachments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id").isNotEmpty
            jsonPath("$.messageId") { value(messageId.toString()) }
            jsonPath("$.fileUrl") { value("https://example.com/file.pdf") }
            jsonPath("$.fileName") { value("document.pdf") }
            jsonPath("$.fileSize") { value(1024) }
            jsonPath("$.mimeType") { value("application/pdf") }
        }
    }

    @Test
    fun testGetAttachmentByMessageIdReturns200() {
        val expectedResponse = MessageAttachmentResponse(
            id = attachmentId,
            messageId = messageId,
            fileUrl = "https://example.com/file.pdf",
            fileName = "document.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf"
        )

        every { messageAttachmentService.getAttachmentByMessageId(messageId) } returns expectedResponse

        mockMvc.get("/api/messages/$messageId/attachments") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(attachmentId.toString()) }
            jsonPath("$.messageId") { value(messageId.toString()) }
            jsonPath("$.fileName") { value("document.pdf") }
        }
    }

    @Test
    fun testGetAttachmentByMessageIdNotFound() {
        every { messageAttachmentService.getAttachmentByMessageId(messageId) } returns null

        mockMvc.get("/api/messages/$messageId/attachments") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun testCreateAttachmentValidationErrorReturns400() {
        val invalidRequest = MessageAttachmentRequest(
            messageId = messageId,
            fileUrl = "",
            fileName = "",
            fileSize = 0L,
            mimeType = ""
        )

        mockMvc.post("/api/messages/$messageId/attachments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Validation Failed") }
        }
    }

    @Test
    fun testCreateAttachmentMessageNotFoundReturns400() {
        val request = MessageAttachmentRequest(
            messageId = messageId,
            fileUrl = "https://example.com/file.pdf",
            fileName = "document.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf"
        )

        every { messageAttachmentService.createAttachment(any()) } throws IllegalArgumentException("Mensaje no encontrado")

        mockMvc.post("/api/messages/$messageId/attachments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
        }
    }
}


