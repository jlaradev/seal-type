package com.jlaradev.sealtype.dto.response

import com.jlaradev.sealtype.enums.ChatType
import java.util.UUID

data class ChatResponse(
    val id: UUID,
    val type: ChatType,
    val title: String? = null,
    val groupIcon: String? = null,
    val isDeleted: Boolean,
    val createdById: UUID
)

