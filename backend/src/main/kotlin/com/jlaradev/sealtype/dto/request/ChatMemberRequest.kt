package com.jlaradev.sealtype.dto.request

import java.util.UUID

data class ChatMemberRequest(
    val userId: UUID? = null,

    val isAdmin: Boolean? = null,

    val isActive: Boolean? = null,

    val lastReadId: UUID? = null
)

