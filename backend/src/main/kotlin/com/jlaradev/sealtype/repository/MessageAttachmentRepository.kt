package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.model.MessageAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageAttachmentRepository : JpaRepository<MessageAttachment, UUID> {
    fun findByMessageId(messageId: UUID): MessageAttachment?
}