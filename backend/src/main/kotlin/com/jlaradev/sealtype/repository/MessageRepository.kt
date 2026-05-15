package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.model.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chatId: UUID, pageable: Pageable): Page<Message>
    fun findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chatId: UUID): Message?
    fun countByChatIdAndCreatedAtAfter(chatId: UUID, createdAt: LocalDateTime): Long
}