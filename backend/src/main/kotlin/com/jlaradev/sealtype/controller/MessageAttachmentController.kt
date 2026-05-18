package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.MessageAttachmentRequest
import com.jlaradev.sealtype.dto.response.MessageAttachmentResponse
import com.jlaradev.sealtype.service.MessageAttachmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/messages")
class MessageAttachmentController(private val messageAttachmentService: MessageAttachmentService) {

    @PostMapping("/{messageId}/attachments")
    fun createAttachment(
        @PathVariable messageId: UUID,
        @Valid @RequestBody request: MessageAttachmentRequest
    ): ResponseEntity<MessageAttachmentResponse> {
        val attachmentRequest = request.copy(messageId = messageId)
        val response = messageAttachmentService.createAttachment(attachmentRequest)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping("/{messageId}/attachments")
    fun getAttachmentByMessageId(@PathVariable messageId: UUID): ResponseEntity<MessageAttachmentResponse> {
        return messageAttachmentService.getAttachmentByMessageId(messageId)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }
}


