package com.jlaradev.sealtype.model

import com.jlaradev.sealtype.enums.MessageType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "messages")
class Message(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat = Chat(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User = User(),

    @Column(columnDefinition = "TEXT")
    val content: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MessageType = MessageType.TEXT,

    val isDeleted: Boolean = false,

    val deletedAt: LocalDateTime? = null,

    @OneToOne(mappedBy = "message", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val attachment: MessageAttachment? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime = LocalDateTime.now()
)