package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.enums.MessageType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.model.MessageAttachment
import com.jlaradev.sealtype.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class MessageAttachmentRepositoryTest @Autowired constructor(
    private val messageAttachmentRepository: MessageAttachmentRepository,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {

    private lateinit var user1: User
    private lateinit var chat1: Chat
    private lateinit var message1: Message
    private lateinit var message2: Message
    private lateinit var attachment1: MessageAttachment

    @BeforeEach
    fun setUp() {
        // Persistencia de usuario
        user1 = userRepository.save(User(username = "user1", passwordHash = "hash1", displayName = "User One"))

        // Persistencia de chat
        chat1 = chatRepository.save(Chat(type = ChatType.GROUP, title = "Grupo 1", createdBy = user1))

        // Persistencia de mensajes: uno con adjunto y otro sin
        message1 = messageRepository.save(Message(chat = chat1, sender = user1, content = "Mensaje con archivo", type = MessageType.TEXT))
        message2 = messageRepository.save(Message(chat = chat1, sender = user1, content = "Mensaje sin archivo", type = MessageType.TEXT))

        // Persistencia de adjunto vinculado al primer mensaje
        attachment1 = messageAttachmentRepository.save(MessageAttachment(message = message1, fileUrl = "https://example.com/file.pdf", fileName = "documento.pdf", fileSize = 1024L, mimeType = "application/pdf"))
    }

    @Test
    fun testFindByMessageId() {
        // Búsqueda de adjunto por ID de mensaje
        val result = messageAttachmentRepository.findByMessageId(message1.id)

        assertThat(result)
            .withFailMessage("Se debe retornar el adjunto vinculado al mensaje")
            .isNotNull
            .hasFieldOrPropertyWithValue("id", attachment1.id)
            .hasFieldOrPropertyWithValue("fileName", "documento.pdf")
            .hasFieldOrPropertyWithValue("fileUrl", "https://example.com/file.pdf")

        // Búsqueda de adjunto para mensaje sin adjunto
        val noAttachment = messageAttachmentRepository.findByMessageId(message2.id)
        assertThat(noAttachment)
            .withFailMessage("Debe retornar null para un mensaje sin adjunto")
            .isNull()
    }
}

