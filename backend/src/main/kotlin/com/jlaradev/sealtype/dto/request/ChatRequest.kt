package com.jlaradev.sealtype.dto.request

import com.jlaradev.sealtype.enums.ChatType
import jakarta.validation.constraints.Size
import java.util.UUID

data class ChatRequest(
    val type: ChatType? = null,

    @field:Size(max = 100, message = "El título debe tener máximo 100 caracteres")
    val title: String? = null,

    val groupIcon: String? = null,

    val memberIds: List<UUID> = emptyList()
)

