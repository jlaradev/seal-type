package com.jlaradev.sealtype.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
data class ChatMemberId(
    @Column(name = "chat_id")
    val chatId: UUID = UUID.randomUUID(),
    @Column(name = "user_id")
    val userId: UUID = UUID.randomUUID()
) : Serializable

@Entity
@Table(name = "chat_members")
class ChatMember(
    @EmbeddedId
    val id: ChatMemberId = ChatMemberId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false, insertable = false, updatable = false)
    val chat: Chat = Chat(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    val user: User = User(),

    val isAdmin: Boolean = false,

    val isActive: Boolean = true,

    val lastReadId: UUID? = null,

    val joinedAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime = LocalDateTime.now()
)