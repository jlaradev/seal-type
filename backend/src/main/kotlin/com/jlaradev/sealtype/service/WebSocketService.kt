package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.websocket.*
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.repository.ChatRepository
import com.jlaradev.sealtype.repository.MessageRepository
import com.jlaradev.sealtype.repository.UserRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class WebSocketService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {

    // Procesa nuevo mensaje y lo retransmite a todos los conectados
    fun handleNewMessage(chatId: UUID, request: WebSocketMessageRequest): NewMessageEvent? {
        try {
            val chat = chatRepository.findById(chatId).orElse(null) ?: return null
            val sender = userRepository.findById(request.userId).orElse(null) ?: return null

            val message = Message(
                chat = chat,
                sender = sender,
                content = request.content,
                type = request.type
            )

            val savedMessage = messageRepository.save(message)

            val event = NewMessageEvent(
                id = savedMessage.id,
                chatId = chatId,
                senderId = sender.id,
                senderName = sender.displayName,
                content = savedMessage.content,
                type = savedMessage.type,
                attachmentId = savedMessage.attachment?.id,
                createdAt = savedMessage.createdAt
            )

            simpMessagingTemplate.convertAndSend(
                "/topic/chat/$chatId/messages",
                event
            )

            return event
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Notifica que un usuario está escribiendo
    fun handleTypingStatus(chatId: UUID, request: TypingStatusRequest) {
        try {
            val user = userRepository.findById(request.userId).orElse(null) ?: return

            val event = TypingEvent(
                userId = user.id,
                userName = user.displayName,
                isTyping = request.isTyping
            )

            simpMessagingTemplate.convertAndSend(
                "/topic/chat/$chatId/typing",
                event
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Notifica cambio de estado online/offline
    fun handleUserStatusChange(request: UserStatusRequest) {
        try {
            val user = userRepository.findById(request.userId).orElse(null) ?: return

            val event = UserStatusEvent(
                userId = user.id,
                userName = user.displayName,
                isOnline = request.isOnline,
                lastSeen = user.lastSeen
            )

            simpMessagingTemplate.convertAndSend(
                "/topic/users/status",
                event
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Notifica que un mensaje fue editado
    fun broadcastMessageEdited(messageId: UUID, content: String, chatId: UUID) {
        try {
            val event = MessageEditedEvent(
                messageId = messageId,
                chatId = chatId,
                content = content,
                updatedAt = LocalDateTime.now()
            )

            simpMessagingTemplate.convertAndSend(
                "/topic/chat/$chatId/messages",
                event
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Notifica que un mensaje fue eliminado
    fun broadcastMessageDeleted(messageId: UUID, chatId: UUID) {
        try {
            val event = MessageDeletedEvent(
                messageId = messageId,
                chatId = chatId
            )

            simpMessagingTemplate.convertAndSend(
                "/topic/chat/$chatId/messages",
                event
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}