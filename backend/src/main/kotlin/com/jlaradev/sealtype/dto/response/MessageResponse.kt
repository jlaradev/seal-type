package com.jlaradev.sealtype.dto.response

import com.jlaradev.sealtype.enums.MessageType
import java.time.LocalDateTime
import java.util.UUID

data class MessageResponse(
    val id: UUID,
    val chatId: UUID,
    val senderId: UUID,
    val content: String,
    val type: MessageType,
    val isDeleted: Boolean,
    val attachmentId: UUID? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)


