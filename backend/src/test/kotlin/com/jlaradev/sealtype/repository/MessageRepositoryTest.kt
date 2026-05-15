package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.enums.MessageType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.Message
import com.jlaradev.sealtype.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@DataJpaTest
class MessageRepositoryTest @Autowired constructor(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var chat1: Chat
    private lateinit var message1: Message
    private lateinit var message2: Message
    private lateinit var message3: Message

    @BeforeEach
    fun setUp() {
        // Persistencia de usuarios
        user1 = userRepository.save(User(username = "user1", passwordHash = "hash1", displayName = "User One"))
        user2 = userRepository.save(User(username = "user2", passwordHash = "hash2", displayName = "User Two"))

        // Persistencia de chat
        chat1 = chatRepository.save(Chat(type = ChatType.GROUP, title = "Grupo 1", createdBy = user1))

        // Persistencia de mensajes: activos y eliminados, con diferentes timestamps
        message1 = messageRepository.save(Message(chat = chat1, sender = user1, content = "Primer mensaje", type = MessageType.TEXT, isDeleted = false, createdAt = LocalDateTime.now().minusHours(2)))
        message2 = messageRepository.save(Message(chat = chat1, sender = user2, content = "Segundo mensaje", type = MessageType.TEXT, isDeleted = false, createdAt = LocalDateTime.now().minusHours(1)))
        message3 = messageRepository.save(Message(chat = chat1, sender = user1, content = "Mensaje eliminado", type = MessageType.TEXT, isDeleted = true, createdAt = LocalDateTime.now().minusMinutes(30)))
    }

    @Test
    fun testFindByChatIdAndIsDeletedFalseOrderByCreatedAtDesc() {
        // Búsqueda de mensajes activos del chat ordenados por fecha descendente
        val pageable = PageRequest.of(0, 10)
        val results = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chat1.id, pageable)

        assertThat(results)
            .withFailMessage("Se deben retornar únicamente los mensajes activos ordenados por fecha descendente")
            .hasSize(2)

        // Verificación del orden: el más reciente primero
        assertThat(results.content.first().id).isEqualTo(message2.id)
        assertThat(results.content.last().id).isEqualTo(message1.id)

        // Verificación de que no incluya mensajes eliminados
        assertThat(results.content)
            .extracting("isDeleted")
            .containsOnly(false)
    }

    @Test
    fun testFindTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc() {
        // Búsqueda del mensaje más reciente activo del chat
        val result = messageRepository.findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chat1.id)

        assertThat(result)
            .withFailMessage("Se debe retornar el mensaje más reciente activo del chat")
            .isNotNull
            .hasFieldOrPropertyWithValue("id", message2.id)
            .hasFieldOrPropertyWithValue("isDeleted", false)
    }

    @Test
    fun testCountByChatIdAndCreatedAtAfter() {
        // Conteo de mensajes creados después de una fecha específica
        val timestamp = LocalDateTime.now().minusHours(3)
        val count = messageRepository.countByChatIdAndCreatedAtAfter(chat1.id, timestamp)

        assertThat(count)
            .withFailMessage("Se deben contar todos los mensajes (3) creados después de la fecha especificada, incluyendo eliminados")
            .isEqualTo(3)

        // Conteo con una fecha más reciente
        val recentCount = messageRepository.countByChatIdAndCreatedAtAfter(chat1.id, LocalDateTime.now().minusMinutes(45))
        assertThat(recentCount)
            .withFailMessage("Se debe contar únicamente 1 mensaje creado después de esa fecha")
            .isEqualTo(1)
    }
}

