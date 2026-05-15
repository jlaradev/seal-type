package com.jlaradev.sealtype.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "message_attachments")
class MessageAttachment(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: Message = Message(),

    val fileUrl: String = "",

    val fileName: String = "",

    val fileSize: Long = 0L,

    val mimeType: String = "",

    @Column(columnDefinition = "TEXT")
    val extraData: String? = null // Metadata como JSON string
)