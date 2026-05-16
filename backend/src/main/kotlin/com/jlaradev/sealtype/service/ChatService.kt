package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.ChatRequest
import com.jlaradev.sealtype.dto.response.ChatResponse
import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.ChatMember
import com.jlaradev.sealtype.model.ChatMemberId
import com.jlaradev.sealtype.repository.ChatMemberRepository
import com.jlaradev.sealtype.repository.ChatRepository
import com.jlaradev.sealtype.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ChatService(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatMemberRepository: ChatMemberRepository
) {

    fun createChat(request: ChatRequest, creatorId: UUID): ChatResponse {
        val creator = userRepository.findById(creatorId).orElseThrow {
            IllegalArgumentException("Creador no encontrado")
        }

        if (request.type == ChatType.GROUP && request.title.isNullOrBlank()) {
            throw IllegalArgumentException("Los chats grupales deben tener un título")
        }

        val chat = Chat(
            type = request.type!!,
            title = request.title,
            groupIcon = request.groupIcon,
            createdBy = creator
        )

        val savedChat = chatRepository.save(chat)

        // Agregar creador como miembro
        val creatorMember = ChatMember(
            id = ChatMemberId(savedChat.id, creatorId),
            chat = savedChat,
            user = creator,
            isAdmin = true,
            isActive = true
        )
        chatMemberRepository.save(creatorMember)

        // Agregar otros miembros si vienen
        request.memberIds.forEach { memberId ->
            if (memberId != creatorId) {
                val member = userRepository.findById(memberId).orElseThrow {
                    IllegalArgumentException("Usuario con ID $memberId no encontrado")
                }
                val chatMember = ChatMember(
                    id = ChatMemberId(savedChat.id, memberId),
                    chat = savedChat,
                    user = member,
                    isAdmin = false,
                    isActive = true
                )
                chatMemberRepository.save(chatMember)
            }
        }

        return toResponse(savedChat)
    }

    fun getChatById(id: UUID): ChatResponse? {
        return chatRepository.findById(id).orElse(null)?.let { toResponse(it) }
    }

    fun updateChat(id: UUID, request: ChatRequest): ChatResponse {
        val chat = chatRepository.findById(id).orElseThrow {
            IllegalArgumentException("Chat no encontrado")
        }

        if (chat.isDeleted) {
            throw IllegalArgumentException("No se puede actualizar un chat eliminado")
        }

        val updatedChat = Chat(
            id = chat.id,
            type = chat.type,
            title = request.title ?: chat.title,
            groupIcon = request.groupIcon ?: chat.groupIcon,
            isDeleted = chat.isDeleted,
            deletedAt = chat.deletedAt,
            createdBy = chat.createdBy,
            createdAt = chat.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedChat = chatRepository.save(updatedChat)
        return toResponse(savedChat)
    }

    fun deleteChat(id: UUID): Unit {
        val chat = chatRepository.findById(id).orElseThrow {
            IllegalArgumentException("Chat no encontrado")
        }

        val deletedChat = Chat(
            id = chat.id,
            type = chat.type,
            title = chat.title,
            groupIcon = chat.groupIcon,
            isDeleted = true,
            deletedAt = LocalDateTime.now(),
            createdBy = chat.createdBy,
            createdAt = chat.createdAt,
            updatedAt = LocalDateTime.now()
        )

        chatRepository.save(deletedChat)
    }

    fun searchChats(searchTerm: String): List<ChatResponse> {
        return chatRepository.findAllByTitleContainingIgnoreCaseAndIsDeletedFalse(searchTerm)
            .map { toResponse(it) }
    }

    private fun toResponse(chat: Chat): ChatResponse {
        return ChatResponse(
            id = chat.id,
            type = chat.type,
            title = chat.title,
            groupIcon = chat.groupIcon,
            isDeleted = chat.isDeleted,
            createdById = chat.createdBy.id
        )
    }
}

