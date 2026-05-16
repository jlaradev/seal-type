package com.jlaradev.sealtype.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class MessageAttachmentRequest(
    val messageId: UUID? = null,

    @field:NotBlank(message = "La URL del archivo no puede estar vacía")
    val fileUrl: String,

    @field:NotBlank(message = "El nombre del archivo no puede estar vacío")
    @field:Size(max = 255, message = "El nombre del archivo debe tener máximo 255 caracteres")
    val fileName: String,

    val fileSize: Long,

    @field:NotBlank(message = "El tipo MIME no puede estar vacío")
    val mimeType: String,

    val extraData: String? = null
)

