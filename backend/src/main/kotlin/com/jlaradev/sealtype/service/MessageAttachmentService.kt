package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.MessageAttachmentRequest
import com.jlaradev.sealtype.dto.response.MessageAttachmentResponse
import com.jlaradev.sealtype.model.MessageAttachment
import com.jlaradev.sealtype.repository.MessageAttachmentRepository
import com.jlaradev.sealtype.repository.MessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class MessageAttachmentService(
    private val messageAttachmentRepository: MessageAttachmentRepository,
    private val messageRepository: MessageRepository
) {

    fun createAttachment(request: MessageAttachmentRequest): MessageAttachmentResponse {
        val message = messageRepository.findById(request.messageId!!).orElseThrow {
            IllegalArgumentException("Mensaje no encontrado")
        }

        val attachment = MessageAttachment(
            message = message,
            fileUrl = request.fileUrl,
            fileName = request.fileName,
            fileSize = request.fileSize,
            mimeType = request.mimeType,
            extraData = request.extraData
        )

        val savedAttachment = messageAttachmentRepository.save(attachment)
        return toResponse(savedAttachment)
    }

    fun getAttachmentByMessageId(messageId: UUID): MessageAttachmentResponse? {
        return messageAttachmentRepository.findByMessageId(messageId)
            ?.let { toResponse(it) }
    }

    private fun toResponse(attachment: MessageAttachment): MessageAttachmentResponse {
        return MessageAttachmentResponse(
            id = attachment.id,
            messageId = attachment.message.id,
            fileUrl = attachment.fileUrl,
            fileName = attachment.fileName,
            fileSize = attachment.fileSize,
            mimeType = attachment.mimeType,
            extraData = attachment.extraData
        )
    }
}

