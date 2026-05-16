package com.jlaradev.sealtype.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isOnline: Boolean,
    val lastSeen: LocalDateTime
)

