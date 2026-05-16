package com.jlaradev.sealtype.dto.response

import java.util.UUID

data class MessageAttachmentResponse(
    val id: UUID,
    val messageId: UUID,
    val fileUrl: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val extraData: String? = null
)

