package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.ChatMemberRequest
import com.jlaradev.sealtype.dto.response.ChatMemberResponse
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
class ChatMemberService(
    private val chatMemberRepository: ChatMemberRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {

    fun addMember(chatId: UUID, request: ChatMemberRequest): ChatMemberResponse {
        val chat = chatRepository.findById(chatId).orElseThrow {
            IllegalArgumentException("Chat no encontrado")
        }

        val user = userRepository.findById(request.userId!!).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        val existingMember = chatMemberRepository.findById(ChatMemberId(chatId, request.userId))
            .orElse(null)

        if (existingMember != null && existingMember.isActive) {
            throw IllegalArgumentException("El usuario ya es miembro del chat")
        }

        val chatMember = if (existingMember != null) {
            // Reactivar miembro inactivo
            ChatMember(
                id = existingMember.id,
                chat = existingMember.chat,
                user = existingMember.user,
                isAdmin = existingMember.isAdmin,
                isActive = true,
                lastReadId = existingMember.lastReadId,
                joinedAt = existingMember.joinedAt,
                updatedAt = LocalDateTime.now()
            )
        } else {
            // Nuevo miembro
            ChatMember(
                id = ChatMemberId(chatId, request.userId),
                chat = chat,
                user = user,
                isAdmin = request.isAdmin ?: false,
                isActive = true
            )
        }

        val savedMember = chatMemberRepository.save(chatMember)
        return toResponse(savedMember)
    }

    fun removeMember(chatId: UUID, userId: UUID): Unit {
        val memberId = ChatMemberId(chatId, userId)
        val member = chatMemberRepository.findById(memberId).orElseThrow {
            IllegalArgumentException("Miembro no encontrado")
        }

        val inactiveMember = ChatMember(
            id = member.id,
            chat = member.chat,
            user = member.user,
            isAdmin = member.isAdmin,
            isActive = false,
            lastReadId = member.lastReadId,
            joinedAt = member.joinedAt,
            updatedAt = LocalDateTime.now()
        )

        chatMemberRepository.save(inactiveMember)
    }

    fun updateMember(chatId: UUID, userId: UUID, request: ChatMemberRequest): ChatMemberResponse {
        val memberId = ChatMemberId(chatId, userId)
        val member = chatMemberRepository.findById(memberId).orElseThrow {
            IllegalArgumentException("Miembro no encontrado")
        }

        val updatedMember = ChatMember(
            id = member.id,
            chat = member.chat,
            user = member.user,
            isAdmin = request.isAdmin ?: member.isAdmin,
            isActive = request.isActive ?: member.isActive,
            lastReadId = request.lastReadId ?: member.lastReadId,
            joinedAt = member.joinedAt,
            updatedAt = LocalDateTime.now()
        )

        val savedMember = chatMemberRepository.save(updatedMember)
        return toResponse(savedMember)
    }

    fun getMembersOfChat(chatId: UUID): List<ChatMemberResponse> {
        return chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(chatId)
            .map { toResponse(it) }
    }

    fun getUserChats(userId: UUID): List<ChatMemberResponse> {
        return chatMemberRepository.findAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse(userId)
            .map { toResponse(it) }
    }

    fun isMemberOfChat(chatId: UUID, userId: UUID): Boolean {
        val memberId = ChatMemberId(chatId, userId)
        return chatMemberRepository.findById(memberId)
            .map { it.isActive }
            .orElse(false)
    }

    private fun toResponse(member: ChatMember): ChatMemberResponse {
        return ChatMemberResponse(
            chatId = member.chat.id,
            userId = member.user.id,
            isAdmin = member.isAdmin,
            isActive = member.isActive,
            lastReadId = member.lastReadId,
            joinedAt = member.joinedAt
        )
    }
}

