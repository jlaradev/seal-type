package com.jlaradev.sealtype.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val username: String = "",

    @Column(nullable = false)
    val passwordHash: String = "",

    val displayName: String = "",

    val avatarUrl: String? = null,

    val isOnline: Boolean = false,

    var lastSeen: LocalDateTime = LocalDateTime.now(),

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun updateTimestamp() {
        this.lastSeen = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }
}
