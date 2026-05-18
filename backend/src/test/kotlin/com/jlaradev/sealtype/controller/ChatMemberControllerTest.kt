package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.ChatMemberRequest
import com.jlaradev.sealtype.dto.response.ChatMemberResponse
import com.jlaradev.sealtype.service.ChatMemberService
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

@WebMvcTest(ChatMemberController::class)
@AutoConfigureMockMvc(addFilters = false)
class ChatMemberControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var chatMemberService: ChatMemberService

    private val chatId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val now = LocalDateTime.now()

    @TestConfiguration
    class TestConfig {
        @Bean
        fun chatMemberService(): ChatMemberService = mockk(relaxed = false)
    }

    @BeforeEach
    fun setup() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun testAddMemberReturns201() {
        val request = ChatMemberRequest(userId = userId)

        val expectedResponse = ChatMemberResponse(
            chatId = chatId,
            userId = userId,
            isAdmin = false,
            isActive = true,
            lastReadId = null,
            joinedAt = now
        )

        every { chatMemberService.addMember(chatId, request) } returns expectedResponse

        mockMvc.post("/api/chats/$chatId/members") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.chatId") { value(chatId.toString()) }
            jsonPath("$.userId") { value(userId.toString()) }
            jsonPath("$.isAdmin") { value(false) }
            jsonPath("$.isActive") { value(true) }
            jsonPath("$.joinedAt").isNotEmpty
        }
    }

    @Test
    fun testRemoveMemberReturns204() {
        io.mockk.justRun { chatMemberService.removeMember(chatId, userId) }

        mockMvc.delete("/api/chats/$chatId/members/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun testUpdateMemberReturns200() {
        val request = ChatMemberRequest(
            userId = userId,
            isAdmin = true
        )

        val expectedResponse = ChatMemberResponse(
            chatId = chatId,
            userId = userId,
            isAdmin = true,
            isActive = true,
            lastReadId = null,
            joinedAt = now
        )

        every { chatMemberService.updateMember(chatId, userId, request) } returns expectedResponse

        mockMvc.put("/api/chats/$chatId/members/$userId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.isAdmin") { value(true) }
        }
    }

    @Test
    fun testGetMembersOfChatReturns200() {
        val member1 = ChatMemberResponse(
            chatId = chatId,
            userId = userId,
            isAdmin = false,
            isActive = true,
            lastReadId = null,
            joinedAt = now
        )
        val member2 = ChatMemberResponse(
            chatId = chatId,
            userId = UUID.randomUUID(),
            isAdmin = true,
            isActive = true,
            lastReadId = null,
            joinedAt = now
        )

        every { chatMemberService.getMembersOfChat(chatId) } returns listOf(member1, member2)

        mockMvc.get("/api/chats/$chatId/members") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$").isArray()
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].isAdmin") { value(false) }
            jsonPath("$[1].isAdmin") { value(true) }
        }
    }

    @Test
    fun testIsMemberOfChatReturns200() {
        every { chatMemberService.isMemberOfChat(chatId, userId) } returns true

        mockMvc.get("/api/chats/$chatId/members/$userId/check") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.isMember") { value(true) }
        }
    }

    @Test
    fun testAddMemberValidationErrorReturns400() {
        val request = ChatMemberRequest(
            userId = userId
        )

        every { chatMemberService.addMember(chatId, request) } throws IllegalArgumentException("Usuario no encontrado")

        mockMvc.post("/api/chats/$chatId/members") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
        }
    }

    @Test
    fun testRemoveMemberNotFoundReturns400() {
        every { chatMemberService.removeMember(chatId, userId) } throws IllegalArgumentException("Miembro no encontrado")

        mockMvc.delete("/api/chats/$chatId/members/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }
}




