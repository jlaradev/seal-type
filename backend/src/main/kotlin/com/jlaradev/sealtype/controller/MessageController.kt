package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.MessageRequest
import com.jlaradev.sealtype.dto.response.MessageResponse
import com.jlaradev.sealtype.service.MessageService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api")
class MessageController(private val messageService: MessageService) {

    @PostMapping("/chats/{chatId}/messages")
    fun sendMessage(
        @PathVariable chatId: UUID,
        @Valid @RequestBody request: MessageRequest,
        @RequestHeader(name = "X-User-Id") senderId: UUID
    ): ResponseEntity<MessageResponse> {
        val messageRequest = request.copy(chatId = chatId)
        val response = messageService.sendMessage(messageRequest, senderId)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping("/messages/{id}")
    fun getMessageById(@PathVariable id: UUID): ResponseEntity<MessageResponse> {
        return messageService.getMessageById(id)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/messages/{id}")
    fun updateMessage(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MessageRequest,
        @RequestHeader(name = "X-User-Id") userId: UUID
    ): ResponseEntity<MessageResponse> {
        val response = messageService.updateMessage(id, request, userId)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @DeleteMapping("/messages/{id}")
    fun deleteMessage(
        @PathVariable id: UUID,
        @RequestHeader(name = "X-User-Id") userId: UUID
    ): ResponseEntity<Unit> {
        messageService.deleteMessage(id, userId)
        return ResponseEntity(Unit, HttpStatus.NO_CONTENT)
    }

    @GetMapping("/chats/{chatId}/messages")
    fun getMessagesByChat(
        @PathVariable chatId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<MessageResponse>> {
        val response = messageService.getMessagesByChat(chatId, pageable)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/chats/{chatId}/messages/last")
    fun getLastMessage(@PathVariable chatId: UUID): ResponseEntity<MessageResponse> {
        return messageService.getLastMessage(chatId)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/chats/{chatId}/messages/count")
    fun countMessagesAfterDate(
        @PathVariable chatId: UUID,
        @RequestParam(name = "after") afterDate: LocalDateTime
    ): ResponseEntity<Map<String, Long>> {
        val count = messageService.countMessagesAfterDate(chatId, afterDate)
        return ResponseEntity(mapOf("count" to count), HttpStatus.OK)
    }
}



