package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.MessageRequest
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.enums.MessageType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.ChatMember
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.model.User
import com.jlaradev.sealtype.repository.ChatMemberRepository
import com.jlaradev.sealtype.repository.ChatRepository
import com.jlaradev.sealtype.repository.MessageRepository
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

class MessageServiceTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatMemberRepository: ChatMemberRepository
    private lateinit var webSocketService: WebSocketService
    private lateinit var messageService: MessageService

    private lateinit var testUser: User
    private lateinit var testChat: Chat
    private lateinit var testMessage: Message
    private val testUserId: UUID = UUID.randomUUID()
    private val testChatId: UUID = UUID.randomUUID()
    private val testMessageId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        messageRepository = mockk()
        chatRepository = mockk()
        userRepository = mockk()
        chatMemberRepository = mockk()

        webSocketService = mockk(relaxed = true)

        messageService = MessageService(messageRepository, chatRepository, userRepository, chatMemberRepository, webSocketService)

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

        testMessage = Message(
            id = testMessageId,
            chat = testChat,
            sender = testUser,
            content = "Hola",
            type = MessageType.TEXT,
            isDeleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun testSendMessageSuccessfully() {
        // Arrange
        val request = MessageRequest(
            chatId = testChatId,
            content = "Hola",
            type = MessageType.TEXT
        )

        var chatMember = mockk<ChatMember>()
        every { chatMember.user.id } returns testUserId

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(testChatId) } returns listOf(chatMember)
        every { messageRepository.save(any()) } returns testMessage

        // Act
        val response = messageService.sendMessage(request, testUserId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("content", "Hola")
            .hasFieldOrPropertyWithValue("type", MessageType.TEXT)

        verify(exactly = 1) { chatRepository.findById(testChatId) }
        verify(exactly = 1) { messageRepository.save(any()) }
    }

    @Test
    fun testSendMessageChatNotFound() {
        // Arrange
        val request = MessageRequest(
            chatId = testChatId,
            content = "Hola",
            type = MessageType.TEXT
        )

        every { chatRepository.findById(testChatId) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy { messageService.sendMessage(request, testUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Chat no encontrado")

        verify(exactly = 0) { messageRepository.save(any()) }
    }

    @Test
    fun testSendMessageUserNotMember() {
        // Arrange
        val request = MessageRequest(
            chatId = testChatId,
            content = "Hola",
            type = MessageType.TEXT
        )

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(testChatId) } returns emptyList()

        // Act & Assert
        assertThatThrownBy { messageService.sendMessage(request, testUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("El usuario no es miembro del chat")

        verify(exactly = 0) { messageRepository.save(any()) }
    }

    @Test
    fun testGetMessageByIdFound() {
        // Arrange
        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)

        // Act
        val response = messageService.getMessageById(testMessageId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testMessageId)
            .hasFieldOrPropertyWithValue("content", "Hola")

        verify(exactly = 1) { messageRepository.findById(testMessageId) }
    }

    @Test
    fun testGetMessageByIdNotFound() {
        // Arrange
        every { messageRepository.findById(testMessageId) } returns Optional.empty()

        // Act
        val response = messageService.getMessageById(testMessageId)

        // Assert
        assertThat(response).isNull()
    }

    @Test
    fun testUpdateMessageSuccessfully() {
        // Arrange
        val updateRequest = MessageRequest(
            content = "Hola actualizado",
            type = MessageType.TEXT
        )

        val updatedMessage = Message(
            id = testMessageId,
            chat = testChat,
            sender = testUser,
            content = "Hola actualizado",
            type = MessageType.TEXT,
            isDeleted = false,
            createdAt = testMessage.createdAt,
            updatedAt = LocalDateTime.now()
        )

        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)
        every { messageRepository.save(any()) } returns updatedMessage

        // Act
        val response = messageService.updateMessage(testMessageId, updateRequest, testUserId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("content", "Hola actualizado")

        verify(exactly = 1) { messageRepository.findById(testMessageId) }
        verify(exactly = 1) { messageRepository.save(any()) }
    }

    @Test
    fun testUpdateMessageNotSender() {
        // Arrange
        val otherUserId = UUID.randomUUID()
        val updateRequest = MessageRequest(
            content = "Hola actualizado",
            type = MessageType.TEXT
        )

        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)

        // Act & Assert
        assertThatThrownBy { messageService.updateMessage(testMessageId, updateRequest, otherUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Solo el remitente puede editar")

        verify(exactly = 0) { messageRepository.save(any()) }
    }

    @Test
    fun testDeleteMessageSuccessfully() {
        // Arrange
        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)
        every { messageRepository.save(any()) } returns mockk()

        // Act
        messageService.deleteMessage(testMessageId, testUserId)

        // Assert
        verify(exactly = 1) { messageRepository.findById(testMessageId) }
        verify(exactly = 1) { messageRepository.save(any()) }
    }

    @Test
    fun testDeleteMessageNotSender() {
        // Arrange
        val otherUserId = UUID.randomUUID()

        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)

        // Act & Assert
        assertThatThrownBy { messageService.deleteMessage(testMessageId, otherUserId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Solo el remitente puede eliminar")

        verify(exactly = 0) { messageRepository.save(any()) }
    }

    @Test
    fun testGetLastMessage() {
        // Arrange
        every { messageRepository.findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(testChatId) } returns testMessage

        // Act
        val response = messageService.getLastMessage(testChatId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testMessageId)

        verify(exactly = 1) { messageRepository.findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(testChatId) }
    }
}



