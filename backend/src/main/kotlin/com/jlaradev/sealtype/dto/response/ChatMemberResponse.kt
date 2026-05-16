package com.jlaradev.sealtype.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class ChatMemberResponse(
    val chatId: UUID,
    val userId: UUID,
    val isAdmin: Boolean,
    val isActive: Boolean,
    val lastReadId: UUID? = null,
    val joinedAt: LocalDateTime
)

