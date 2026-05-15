package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.model.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChatRepository : JpaRepository<Chat, UUID> {
    fun findAllByTitleContainingIgnoreCaseAndIsDeletedFalse(title: String): List<Chat>
}