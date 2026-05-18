package com.jlaradev.sealtype.dto.websocket

import com.jlaradev.sealtype.enums.MessageType
import java.time.LocalDateTime
import java.util.UUID

// client events

data class WebSocketMessageRequest(
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val userId: UUID
)

data class TypingStatusRequest(
    val userId: UUID,
    val isTyping: Boolean
)

data class UserStatusRequest(
    val userId: UUID,
    val isOnline: Boolean
)

// server events

data class NewMessageEvent(
    val id: UUID,
    val chatId: UUID,
    val senderId: UUID,
    val senderName: String,
    val content: String,
    val type: MessageType,
    val attachmentId: UUID?,
    val createdAt: LocalDateTime,
    val eventType: String = "NEW_MESSAGE"
)

data class TypingEvent(
    val userId: UUID,
    val userName: String,
    val isTyping: Boolean,
    val eventType: String = "USER_TYPING"
)

data class UserStatusEvent(
    val userId: UUID,
    val userName: String,
    val isOnline: Boolean,
    val lastSeen: LocalDateTime,
    val eventType: String = "USER_STATUS_CHANGED"
)

data class MessageEditedEvent(
    val messageId: UUID,
    val chatId: UUID,
    val content: String,
    val updatedAt: LocalDateTime,
    val eventType: String = "MESSAGE_EDITED"
)

data class MessageDeletedEvent(
    val messageId: UUID,
    val chatId: UUID,
    val eventType: String = "MESSAGE_DELETED"
)