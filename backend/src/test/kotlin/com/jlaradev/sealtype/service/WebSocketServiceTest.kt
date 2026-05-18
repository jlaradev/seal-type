package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.websocket.*
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.enums.MessageType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.model.User
import com.jlaradev.sealtype.repository.ChatRepository
import com.jlaradev.sealtype.repository.MessageRepository
import com.jlaradev.sealtype.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class WebSocketServiceTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var simpMessagingTemplate: SimpMessagingTemplate
    private lateinit var webSocketService: WebSocketService

    private lateinit var testUser: User
    private lateinit var testChat: Chat
    private lateinit var testMessage: Message
    private val testUserId: UUID = UUID.randomUUID()
    private val testChatId: UUID = UUID.randomUUID()
    private val testMessageId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        messageRepository = mockk()
        userRepository = mockk()
        chatRepository = mockk()
        simpMessagingTemplate = mockk(relaxed = true)

        webSocketService = WebSocketService(
            simpMessagingTemplate,
            messageRepository,
            userRepository,
            chatRepository
        )

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
            type = ChatType.DIRECT,
            title = null,
            createdBy = testUser,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testMessage = Message(
            id = testMessageId,
            chat = testChat,
            sender = testUser,
            content = "Test message",
            type = MessageType.TEXT,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun testHandleNewMessageSavesAndBroadcasts() {
        val request = WebSocketMessageRequest(
            content = "Test message",
            type = MessageType.TEXT,
            userId = testUserId
        )

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { messageRepository.save(ofType(Message::class)) } returns testMessage
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(NewMessageEvent::class)) }.returns(Unit)

        val result = webSocketService.handleNewMessage(testChatId, request)

        assertThat(result).isNotNull()
        assertThat(result?.content).isEqualTo("Test message")
        assertThat(result?.senderId).isEqualTo(testUserId)
        assertThat(result?.chatId).isEqualTo(testChatId)
        assertThat(result?.eventType).isEqualTo("NEW_MESSAGE")

        verify { messageRepository.save(ofType(Message::class)) }
        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(NewMessageEvent::class)) }
    }

    @Test
    fun testHandleNewMessageReturnNullIfChatNotFound() {
        val request = WebSocketMessageRequest(
            content = "Test message",
            type = MessageType.TEXT,
            userId = testUserId
        )

        every { chatRepository.findById(testChatId) } returns Optional.empty()

        assertThat(webSocketService.handleNewMessage(testChatId, request)).isNull()
        verify(exactly = 0) { messageRepository.save(ofType(Message::class)) }
    }

    @Test
    fun testHandleNewMessageReturnNullIfUserNotFound() {
        val request = WebSocketMessageRequest(
            content = "Test message",
            type = MessageType.TEXT,
            userId = testUserId
        )

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.empty()

        assertThat(webSocketService.handleNewMessage(testChatId, request)).isNull()
        verify(exactly = 0) { messageRepository.save(ofType(Message::class)) }
    }

    @Test
    fun testHandleNewMessagePreservesMessageType() {
        val request = WebSocketMessageRequest(
            content = "Image message",
            type = MessageType.IMAGE,
            userId = testUserId
        )

        val imageMessage = Message(
            id = testMessageId,
            chat = testChat,
            sender = testUser,
            content = "Image message",
            type = MessageType.IMAGE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { chatRepository.findById(testChatId) } returns Optional.of(testChat)
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { messageRepository.save(ofType(Message::class)) } returns imageMessage
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(NewMessageEvent::class)) }.returns(Unit)

        val result = webSocketService.handleNewMessage(testChatId, request)

        assertThat(result?.type).isEqualTo(MessageType.IMAGE)
    }

    @Test
    fun testHandleTypingStatusBroadcastsEvent() {
        val request = TypingStatusRequest(userId = testUserId, isTyping = true)

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(TypingEvent::class)) }.returns(Unit)

        webSocketService.handleTypingStatus(testChatId, request)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(TypingEvent::class)) }
    }

    @Test
    fun testHandleTypingStatusHandlesUserNotFound() {
        val request = TypingStatusRequest(userId = testUserId, isTyping = true)

        every { userRepository.findById(testUserId) } returns Optional.empty()

        webSocketService.handleTypingStatus(testChatId, request)

        verify(exactly = 0) { simpMessagingTemplate.convertAndSend(any<String>(), ofType(Any::class)) }
    }

    @Test
    fun testHandleTypingStatusBroadcastsWhenStopsTyping() {
        val request = TypingStatusRequest(userId = testUserId, isTyping = false)

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(TypingEvent::class)) }.returns(Unit)

        webSocketService.handleTypingStatus(testChatId, request)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(TypingEvent::class)) }
    }

    @Test
    fun testHandleUserStatusChangeBroadcastsOnline() {
        val request = UserStatusRequest(userId = testUserId, isOnline = true)

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(UserStatusEvent::class)) }.returns(Unit)

        webSocketService.handleUserStatusChange(request)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(UserStatusEvent::class)) }
    }

    @Test
    fun testHandleUserStatusChangeBroadcastsOffline() {
        val request = UserStatusRequest(userId = testUserId, isOnline = false)

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(UserStatusEvent::class)) }.returns(Unit)

        webSocketService.handleUserStatusChange(request)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(UserStatusEvent::class)) }
    }

    @Test
    fun testHandleUserStatusChangeHandlesUserNotFound() {
        val request = UserStatusRequest(userId = testUserId, isOnline = true)

        every { userRepository.findById(testUserId) } returns Optional.empty()

        webSocketService.handleUserStatusChange(request)

        verify(exactly = 0) { simpMessagingTemplate.convertAndSend(any<String>(), ofType(Any::class)) }
    }

    @Test
    fun testBroadcastMessageEditedSendsEvent() {
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(MessageEditedEvent::class)) }.returns(Unit)

        webSocketService.broadcastMessageEdited(testMessageId, "Updated content", testChatId)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(MessageEditedEvent::class)) }
    }

    @Test
    fun testBroadcastMessageEditedHandlesException() {
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(Any::class)) } throws RuntimeException("Broadcast failed")

        webSocketService.broadcastMessageEdited(testMessageId, "content", testChatId)
    }

    @Test
    fun testBroadcastMessageDeletedSendsEvent() {
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(MessageDeletedEvent::class)) }.returns(Unit)

        webSocketService.broadcastMessageDeleted(testMessageId, testChatId)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(MessageDeletedEvent::class)) }
    }

    @Test
    fun testBroadcastMessageDeletedHandlesException() {
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(Any::class)) } throws RuntimeException("Broadcast failed")

        webSocketService.broadcastMessageDeleted(testMessageId, testChatId)
    }

    @Test
    fun testMultipleEventsBroadcastIndependently() {
        val userRequest = UserStatusRequest(userId = testUserId, isOnline = true)
        val typingRequest = TypingStatusRequest(userId = testUserId, isTyping = true)

        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(UserStatusEvent::class)) }.returns(Unit)
        every { simpMessagingTemplate.convertAndSend(any<String>(), ofType(TypingEvent::class)) }.returns(Unit)

        webSocketService.handleUserStatusChange(userRequest)
        webSocketService.handleTypingStatus(testChatId, typingRequest)

        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(UserStatusEvent::class)) }
        verify { simpMessagingTemplate.convertAndSend(any<String>(), ofType(TypingEvent::class)) }
    }
}