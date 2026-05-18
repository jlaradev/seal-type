package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.request.ChatMemberRequest
import com.jlaradev.sealtype.dto.response.ChatMemberResponse
import com.jlaradev.sealtype.service.ChatMemberService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/chats")
class ChatMemberController(private val chatMemberService: ChatMemberService) {

    @PostMapping("/{chatId}/members")
    fun addMember(
        @PathVariable chatId: UUID,
        @Valid @RequestBody request: ChatMemberRequest
    ): ResponseEntity<ChatMemberResponse> {
        val response = chatMemberService.addMember(chatId, request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @DeleteMapping("/{chatId}/members/{userId}")
    fun removeMember(
        @PathVariable chatId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<Unit> {
        chatMemberService.removeMember(chatId, userId)
        return ResponseEntity(Unit, HttpStatus.NO_CONTENT)
    }

    @PutMapping("/{chatId}/members/{userId}")
    fun updateMember(
        @PathVariable chatId: UUID,
        @PathVariable userId: UUID,
        @Valid @RequestBody request: ChatMemberRequest
    ): ResponseEntity<ChatMemberResponse> {
        val response = chatMemberService.updateMember(chatId, userId, request)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/{chatId}/members")
    fun getMembersOfChat(@PathVariable chatId: UUID): ResponseEntity<List<ChatMemberResponse>> {
        val response = chatMemberService.getMembersOfChat(chatId)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/{chatId}/members/{userId}/check")
    fun isMemberOfChat(
        @PathVariable chatId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val isMember = chatMemberService.isMemberOfChat(chatId, userId)
        return ResponseEntity(mapOf("isMember" to isMember), HttpStatus.OK)
    }
}

@RestController
@RequestMapping("/api/users")
class UserChatsController(private val chatMemberService: ChatMemberService) {

    @GetMapping("/{userId}/chats")
    fun getUserChats(@PathVariable userId: UUID): ResponseEntity<List<ChatMemberResponse>> {
        val response = chatMemberService.getUserChats(userId)
        return ResponseEntity(response, HttpStatus.OK)
    }
}

