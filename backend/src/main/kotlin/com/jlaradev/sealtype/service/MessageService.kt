package com.jlaradev.sealtype.service

import com.jlaradev.sealtype.dto.request.MessageRequest
import com.jlaradev.sealtype.dto.response.MessageResponse
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.repository.ChatMemberRepository
import com.jlaradev.sealtype.repository.ChatRepository
import com.jlaradev.sealtype.repository.MessageRepository
import com.jlaradev.sealtype.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class MessageService(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatMemberRepository: ChatMemberRepository
) {

    fun sendMessage(request: MessageRequest, senderId: UUID): MessageResponse {
        val chat = chatRepository.findById(request.chatId!!).orElseThrow {
            IllegalArgumentException("Chat no encontrado")
        }

        if (chat.isDeleted) {
            throw IllegalArgumentException("No se puede enviar mensajes a un chat eliminado")
        }

        val sender = userRepository.findById(senderId).orElseThrow {
            IllegalArgumentException("Remitente no encontrado")
        }

        val isMember = chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(chat.id)
            .any { it.user.id == senderId }

        if (!isMember) {
            throw IllegalArgumentException("El usuario no es miembro del chat")
        }

        val message = Message(
            chat = chat,
            sender = sender,
            content = request.content,
            type = request.type!!
        )

        val savedMessage = messageRepository.save(message)
        return toResponse(savedMessage)
    }

    fun getMessageById(id: UUID): MessageResponse? {
        return messageRepository.findById(id).orElse(null)?.let { toResponse(it) }
    }

    fun updateMessage(id: UUID, request: MessageRequest, userId: UUID): MessageResponse {
        val message = messageRepository.findById(id).orElseThrow {
            IllegalArgumentException("Mensaje no encontrado")
        }

        if (message.sender.id != userId) {
            throw IllegalArgumentException("Solo el remitente puede editar el mensaje")
        }

        if (message.isDeleted) {
            throw IllegalArgumentException("No se puede editar un mensaje eliminado")
        }

        val updatedMessage = Message(
            id = message.id,
            chat = message.chat,
            sender = message.sender,
            content = request.content,
            type = message.type,
            isDeleted = message.isDeleted,
            deletedAt = message.deletedAt,
            attachment = message.attachment,
            createdAt = message.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedMessage = messageRepository.save(updatedMessage)
        return toResponse(savedMessage)
    }

    fun deleteMessage(id: UUID, userId: UUID): Unit {
        val message = messageRepository.findById(id).orElseThrow {
            IllegalArgumentException("Mensaje no encontrado")
        }

        if (message.sender.id != userId) {
            throw IllegalArgumentException("Solo el remitente puede eliminar el mensaje")
        }

        val deletedMessage = Message(
            id = message.id,
            chat = message.chat,
            sender = message.sender,
            content = message.content,
            type = message.type,
            isDeleted = true,
            deletedAt = LocalDateTime.now(),
            attachment = message.attachment,
            createdAt = message.createdAt,
            updatedAt = LocalDateTime.now()
        )

        messageRepository.save(deletedMessage)
    }

    fun getMessagesByChat(chatId: UUID, pageable: Pageable): Page<MessageResponse> {
        val chat = chatRepository.findById(chatId).orElseThrow {
            IllegalArgumentException("Chat no encontrado")
        }

        return messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chatId, pageable)
            .map { toResponse(it) }
    }

    fun getLastMessage(chatId: UUID): MessageResponse? {
        return messageRepository.findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chatId)
            ?.let { toResponse(it) }
    }

    fun countMessagesAfterDate(chatId: UUID, afterDate: LocalDateTime): Long {
        return messageRepository.countByChatIdAndCreatedAtAfter(chatId, afterDate)
    }

    private fun toResponse(message: Message): MessageResponse {
        return MessageResponse(
            id = message.id,
            chatId = message.chat.id,
            senderId = message.sender.id,
            content = message.content,
            type = message.type,
            isDeleted = message.isDeleted,
            attachmentId = message.attachment?.id,
            createdAt = message.createdAt,
            updatedAt = message.updatedAt
        )
    }
}

