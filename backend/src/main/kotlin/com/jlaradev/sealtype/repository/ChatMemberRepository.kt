package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.model.ChatMember
import com.jlaradev.sealtype.model.ChatMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChatMemberRepository : JpaRepository<ChatMember, ChatMemberId> {
    fun findAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse(userId: UUID): List<ChatMember>
    fun findAllByIdChatIdAndIsActiveTrue(chatId: UUID): List<ChatMember>
}