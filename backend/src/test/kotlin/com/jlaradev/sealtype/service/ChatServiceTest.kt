package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.ChatRequest
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.model.Chat
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

class ChatServiceTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatMemberRepository: ChatMemberRepository
    private lateinit var chatService: ChatService

    private lateinit var testUser: User
    private lateinit var testChat: Chat
    private val testUserId: UUID = UUID.randomUUID()
    private val testChatId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        chatRepository = mockk()
        userRepository = mockk()
        chatMemberRepository = mockk()
        chatService = ChatService(chatRepository, userRepository, chatMemberRepository)

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
            groupIcon = null,
            isDeleted = false,
            deletedAt = null,
            createdBy = testUser,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun testCreateChatSuccessfully() {
        // Arrange
        val request = ChatRequest(
            type = ChatType.GROUP,
            title = "Grupo 1",
            groupIcon = null,
            memberIds = emptyList()
        )

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { chatRepository.save(any()) } returns testChat
        every { chatMemberRepository.save(any()) } returns mockk()

        // Act
        val response = chatService.createChat(request, testUserId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("type", ChatType.GROUP)
            .hasFieldOrPropertyWithValue("title", "Grupo 1")

        verify(exactly = 1) { userRepository.findById(testUserId) }
        verify(exactly = 1) { chatRepository.save(any()) }
    }

    @Test
    fun testCreateGroupChatWithoutTitle() {
        // Arrange
        val request = ChatRequest(
            type = ChatType.GROUP,
            title = null,
            groupIcon = null,
            memberIds = emptyList()
        )

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)

        // Act & Assert
        assertThatThrownBy { chatService.createChat(request, testUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Los chats grupales deben tener un título")

        verify(exactly = 0) { chatRepository.save(any()) }
    }

    @Test
    fun testCreateChatCreatorNotFound() {
        // Arrange
        val request = ChatRequest(
            type = ChatType.DIRECT,
            memberIds = emptyList()
        )

        every { userRepository.findById(testUserId) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy { chatService.createChat(request, testUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Creador no encontrado")

        verify(exactly = 0) { chatRepository.save(any()) }
    }

    @Test
    fun testGetChatByIdFound() {
        // Arrange
        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)

        // Act
        val response = chatService.getChatById(testChatId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testChatId)
            .hasFieldOrPropertyWithValue("type", ChatType.GROUP)

        verify(exactly = 1) { chatRepository.findById(testChatId) }
    }

    @Test
    fun testGetChatByIdNotFound() {
        // Arrange
        every { chatRepository.findById(testChatId) } returns Optional.empty()

        // Act
        val response = chatService.getChatById(testChatId)

        // Assert
        assertThat(response).isNull()
    }

    @Test
    fun testUpdateChatSuccessfully() {
        // Arrange
        val updateRequest = ChatRequest(
            title = "Grupo Updated",
            groupIcon = "https://example.com/icon.jpg"
        )

        val updatedChat = Chat(
            id = testChatId,
            type = ChatType.GROUP,
            title = "Grupo Updated",
            groupIcon = "https://example.com/icon.jpg",
            isDeleted = false,
            deletedAt = null,
            createdBy = testUser,
            createdAt = testChat.createdAt,
            updatedAt = LocalDateTime.now()
        )

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { chatRepository.save(any()) } returns updatedChat

        // Act
        val response = chatService.updateChat(testChatId, updateRequest)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("title", "Grupo Updated")

        verify(exactly = 1) { chatRepository.findById(testChatId) }
        verify(exactly = 1) { chatRepository.save(any()) }
    }

    @Test
    fun testUpdateDeletedChat() {
        // Arrange
        val deletedChat = Chat(
            id = testChatId,
            type = ChatType.GROUP,
            title = "Grupo 1",
            isDeleted = true,
            deletedAt = LocalDateTime.now(),
            createdBy = testUser,
            createdAt = testChat.createdAt,
            updatedAt = testChat.updatedAt
        )
        val updateRequest = ChatRequest(title = "Updated")

        every { chatRepository.findById(testChatId) } returns Optional.of(deletedChat)

        // Act & Assert
        assertThatThrownBy { chatService.updateChat(testChatId, updateRequest) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No se puede actualizar un chat eliminado")

        verify(exactly = 0) { chatRepository.save(any()) }
    }

    @Test
    fun testDeleteChatSuccessfully() {
        // Arrange
        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { chatRepository.save(any()) } returns mockk()

        // Act
        chatService.deleteChat(testChatId)

        // Assert
        verify(exactly = 1) { chatRepository.findById(testChatId) }
        verify(exactly = 1) { chatRepository.save(any()) }
    }

    @Test
    fun testSearchChats() {
        // Arrange
        val chat2 = Chat(
            id = UUID.randomUUID(),
            type = ChatType.GROUP,
            title = "Grupo Secundario",
            isDeleted = false,
            createdBy = testUser
        )

        every { chatRepository.findAllByTitleContainingIgnoreCaseAndIsDeletedFalse("Grupo") } returns listOf(testChat, chat2)

        // Act
        val results = chatService.searchChats("Grupo")

        // Assert
        assertThat(results)
            .hasSize(2)
            .extracting("title")
            .containsExactlyInAnyOrder("Grupo 1", "Grupo Secundario")

        verify(exactly = 1) { chatRepository.findAllByTitleContainingIgnoreCaseAndIsDeletedFalse("Grupo") }
    }
}


