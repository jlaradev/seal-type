package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.ChatMemberRequest
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.ChatMember
import com.jlaradev.sealtype.model.ChatMemberId
import com.jlaradev.sealtype.model.User
import com.jlaradev.sealtype.repository.ChatMemberRepository
import com.jlaradev.sealtype.repository.ChatRepository
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

class ChatMemberServiceTest {

    private lateinit var chatMemberRepository: ChatMemberRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var chatMemberService: ChatMemberService

    private lateinit var testUser: User
    private lateinit var testChat: Chat
    private lateinit var testChatMember: ChatMember
    private val testUserId: UUID = UUID.randomUUID()
    private val testChatId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        chatMemberRepository = mockk()
        userRepository = mockk()
        chatRepository = mockk()
        chatMemberService = ChatMemberService(chatMemberRepository, userRepository, chatRepository)

        testUser = User(
            id = testUserId,
            username = "user1",
            passwordHash = "hash123",
            displayName = "User One",
            isOnline = true,
            lastSeen = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testChat = Chat(
            id = testChatId,
            type = ChatType.GROUP,
            title = "Grupo 1",
            isDeleted = false,
            createdBy = testUser
        )

        testChatMember = ChatMember(
            id = ChatMemberId(testChatId, testUserId),
            chat = testChat,
            user = testUser,
            isAdmin = false,
            isActive = true,
            joinedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun testAddMemberSuccessfully() {
        // Arrange
        val request = ChatMemberRequest(
            userId = testUserId,
            isAdmin = false
        )

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.empty()
        every { chatMemberRepository.save(any()) } returns testChatMember

        // Act
        val response = chatMemberService.addMember(testChatId, request)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("userId", testUserId)
            .hasFieldOrPropertyWithValue("isActive", true)

        verify(exactly = 1) { chatRepository.findById(testChatId) }
        verify(exactly = 1) { userRepository.findById(testUserId) }
        verify(exactly = 1) { chatMemberRepository.save(any()) }
    }

    @Test
    fun testAddMemberChatNotFound() {
        // Arrange
        val request = ChatMemberRequest(
            userId = testUserId,
            isAdmin = false
        )

        every { chatRepository.findById(testChatId) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy { chatMemberService.addMember(testChatId, request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Chat no encontrado")

        verify(exactly = 0) { chatMemberRepository.save(any()) }
    }

    @Test
    fun testAddMemberUserAlreadyActive() {
        // Arrange
        val request = ChatMemberRequest(userId = testUserId)

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.of(testChatMember)

        // Act & Assert
        assertThatThrownBy { chatMemberService.addMember(testChatId, request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("El usuario ya es miembro del chat")

        verify(exactly = 0) { chatMemberRepository.save(any()) }
    }

    @Test
    fun testRemoveMemberSuccessfully() {
        // Arrange
        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.of(testChatMember)
        every { chatMemberRepository.save(any()) } returns mockk()

        // Act
        chatMemberService.removeMember(testChatId, testUserId)

        // Assert
        verify(exactly = 1) { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) }
        verify(exactly = 1) { chatMemberRepository.save(any()) }
    }

    @Test
    fun testRemoveMemberNotFound() {
        // Arrange
        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy { chatMemberService.removeMember(testChatId, testUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Miembro no encontrado")

        verify(exactly = 0) { chatMemberRepository.save(any()) }
    }

    @Test
    fun testUpdateMemberSuccessfully() {
        // Arrange
        val updateRequest = ChatMemberRequest(
            isAdmin = true,
            isActive = true,
            lastReadId = UUID.randomUUID()
        )

        val updatedMember = ChatMember(
            id = testChatMember.id,
            chat = testChatMember.chat,
            user = testChatMember.user,
            isAdmin = true,
            isActive = true,
            lastReadId = updateRequest.lastReadId,
            joinedAt = testChatMember.joinedAt,
            updatedAt = LocalDateTime.now()
        )

        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.of(testChatMember)
        every { chatMemberRepository.save(any()) } returns updatedMember

        // Act
        val response = chatMemberService.updateMember(testChatId, testUserId, updateRequest)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("isAdmin", true)

        verify(exactly = 1) { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) }
        verify(exactly = 1) { chatMemberRepository.save(any()) }
    }

    @Test
    fun testGetMembersOfChat() {
        // Arrange
        val members = listOf(testChatMember)
        every { chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(testChatId) } returns members

        // Act
        val results = chatMemberService.getMembersOfChat(testChatId)

        // Assert
        assertThat(results)
            .hasSize(1)
            .extracting("userId")
            .containsExactly(testUserId)

        verify(exactly = 1) { chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(testChatId) }
    }

    @Test
    fun testGetUserChats() {
        // Arrange
        val members = listOf(testChatMember)
        every { chatMemberRepository.findAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse(testUserId) } returns members

        // Act
        val results = chatMemberService.getUserChats(testUserId)

        // Assert
        assertThat(results)
            .hasSize(1)
            .extracting("chatId")
            .containsExactly(testChatId)

        verify(exactly = 1) { chatMemberRepository.findAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse(testUserId) }
    }

    @Test
    fun testIsMemberOfChat() {
        // Arrange
        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.of(testChatMember)

        // Act
        val isMember = chatMemberService.isMemberOfChat(testChatId, testUserId)

        // Assert
        assertThat(isMember).isTrue()
    }

    @Test
    fun testIsNotMemberOfChat() {
        // Arrange
        every { chatMemberRepository.findById(ChatMemberId(testChatId, testUserId)) } returns Optional.empty()

        // Act
        val isMember = chatMemberService.isMemberOfChat(testChatId, testUserId)

        // Assert
        assertThat(isMember).isFalse()
    }
}

