package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.ChatRequest
import com.jlaradev.sealtype.dto.response.ChatResponse
import com.jlaradev.sealtype.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/chats")
class ChatController(private val chatService: ChatService) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody request: ChatRequest,
        @RequestHeader(name = "X-User-Id") creatorId: UUID
    ): ResponseEntity<ChatResponse> {
        val response = chatService.createChat(request, creatorId)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getChatById(@PathVariable id: UUID): ResponseEntity<ChatResponse> {
        return chatService.getChatById(id)?.let {
            ResponseEntity(it, HttpStatus.OK)
        } ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}")
    fun updateChat(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChatRequest
    ): ResponseEntity<ChatResponse> {
        val response = chatService.updateChat(id, request)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: UUID): ResponseEntity<Unit> {
        chatService.deleteChat(id)
        return ResponseEntity(Unit, HttpStatus.NO_CONTENT)
    }

    @GetMapping("/search")
    fun searchChats(@RequestParam(name = "term") searchTerm: String): ResponseEntity<List<ChatResponse>> {
        val response = chatService.searchChats(searchTerm)
        return ResponseEntity(response, HttpStatus.OK)
    }
}


