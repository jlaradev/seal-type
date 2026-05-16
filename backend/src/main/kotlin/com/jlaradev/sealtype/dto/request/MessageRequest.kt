package com.jlaradev.sealtype.dto.request

import com.jlaradev.sealtype.enums.MessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class MessageRequest(
    val chatId: UUID? = null,

    @field:NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @field:Size(max = 5000, message = "El contenido del mensaje debe tener máximo 5000 caracteres")
    val content: String,

    val type: MessageType? = null
)


