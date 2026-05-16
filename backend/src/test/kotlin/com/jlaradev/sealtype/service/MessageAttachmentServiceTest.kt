package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.MessageAttachmentRequest
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.enums.MessageType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.model.MessageAttachment
import com.jlaradev.sealtype.model.User
import com.jlaradev.sealtype.repository.MessageAttachmentRepository
import com.jlaradev.sealtype.repository.MessageRepository
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

class MessageAttachmentServiceTest {

    private lateinit var messageAttachmentRepository: MessageAttachmentRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var messageAttachmentService: MessageAttachmentService

    private lateinit var testUser: User
    private lateinit var testChat: Chat
    private lateinit var testMessage: Message
    private lateinit var testAttachment: MessageAttachment
    private val testUserId: UUID = UUID.randomUUID()
    private val testChatId: UUID = UUID.randomUUID()
    private val testMessageId: UUID = UUID.randomUUID()
    private val testAttachmentId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        messageAttachmentRepository = mockk()
        messageRepository = mockk()
        messageAttachmentService = MessageAttachmentService(messageAttachmentRepository, messageRepository)

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
            content = "Archivo adjunto",
            type = MessageType.FILE,
            isDeleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testAttachment = MessageAttachment(
            id = testAttachmentId,
            message = testMessage,
            fileUrl = "https://example.com/file.pdf",
            fileName = "documento.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf",
            extraData = null
        )
    }

    @Test
    fun testCreateAttachmentSuccessfully() {
        // Arrange
        val request = MessageAttachmentRequest(
            messageId = testMessageId,
            fileUrl = "https://example.com/file.pdf",
            fileName = "documento.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf",
            extraData = null
        )

        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)
        every { messageAttachmentRepository.save(any()) } returns testAttachment

        // Act
        val response = messageAttachmentService.createAttachment(request)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testAttachmentId)
            .hasFieldOrPropertyWithValue("fileName", "documento.pdf")
            .hasFieldOrPropertyWithValue("fileSize", 1024L)

        verify(exactly = 1) { messageRepository.findById(testMessageId) }
        verify(exactly = 1) { messageAttachmentRepository.save(any()) }
    }

    @Test
    fun testCreateAttachmentMessageNotFound() {
        // Arrange
        val request = MessageAttachmentRequest(
            messageId = testMessageId,
            fileUrl = "https://example.com/file.pdf",
            fileName = "documento.pdf",
            fileSize = 1024L,
            mimeType = "application/pdf"
        )

        every { messageRepository.findById(testMessageId) } returns Optional.empty()

        // Act & Assert
        assertThatThrownBy { messageAttachmentService.createAttachment(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Mensaje no encontrado")

        verify(exactly = 0) { messageAttachmentRepository.save(any()) }
    }

    @Test
    fun testGetAttachmentByMessageIdFound() {
        // Arrange
        every { messageAttachmentRepository.findByMessageId(testMessageId) } returns testAttachment

        // Act
        val response = messageAttachmentService.getAttachmentByMessageId(testMessageId)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("id", testAttachmentId)
            .hasFieldOrPropertyWithValue("messageId", testMessageId)
            .hasFieldOrPropertyWithValue("fileName", "documento.pdf")

        verify(exactly = 1) { messageAttachmentRepository.findByMessageId(testMessageId) }
    }

    @Test
    fun testGetAttachmentByMessageIdNotFound() {
        // Arrange
        every { messageAttachmentRepository.findByMessageId(testMessageId) } returns null

        // Act
        val response = messageAttachmentService.getAttachmentByMessageId(testMessageId)

        // Assert
        assertThat(response).isNull()

        verify(exactly = 1) { messageAttachmentRepository.findByMessageId(testMessageId) }
    }

    @Test
    fun testCreateAttachmentWithExtraData() {
        // Arrange
        val extraData = """{"width": 1920, "height": 1080}"""
        val request = MessageAttachmentRequest(
            messageId = testMessageId,
            fileUrl = "https://example.com/image.jpg",
            fileName = "imagen.jpg",
            fileSize = 2048L,
            mimeType = "image/jpeg",
            extraData = extraData
        )

        val attachmentWithExtraData = MessageAttachment(
            id = testAttachmentId,
            message = testMessage,
            fileUrl = "https://example.com/image.jpg",
            fileName = "imagen.jpg",
            fileSize = 2048L,
            mimeType = "image/jpeg",
            extraData = extraData
        )

        every { messageRepository.findById(testMessageId) } returns Optional.of(testMessage)
        every { messageAttachmentRepository.save(any()) } returns attachmentWithExtraData

        // Act
        val response = messageAttachmentService.createAttachment(request)

        // Assert
        assertThat(response)
            .isNotNull
            .hasFieldOrPropertyWithValue("extraData", extraData)
            .hasFieldOrPropertyWithValue("mimeType", "image/jpeg")

        verify(exactly = 1) { messageAttachmentRepository.save(any()) }
    }
}

