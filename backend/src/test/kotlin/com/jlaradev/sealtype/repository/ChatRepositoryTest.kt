package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.model.Chat
import com.jlaradev.sealtype.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class ChatRepositoryTest @Autowired constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {

    private lateinit var user1: User
    private lateinit var chat1: Chat
    private lateinit var chat2: Chat
    private lateinit var chat3: Chat

    @BeforeEach
    fun setUp() {
        // Persistencia de usuario creador de chats
        user1 = userRepository.save(User(username = "user1", passwordHash = "hash1", displayName = "User One"))

        // Persistencia de chats: algunos activos, otros eliminados, con títulos variados
        chat1 = chatRepository.save(Chat(type = ChatType.GROUP, title = "Proyecto Kotlin", createdBy = user1, isDeleted = false))
        chat2 = chatRepository.save(Chat(type = ChatType.GROUP, title = "Proyecto kotlin eliminado", createdBy = user1, isDeleted = true))
        chat3 = chatRepository.save(Chat(type = ChatType.DIRECT, title = "Chat Directo", createdBy = user1, isDeleted = false))
    }

    @Test
    fun testFindAllByTitleContainingIgnoreCaseAndIsDeletedFalse() {
        // Ejecución de búsqueda por título "kotlin" sin diferenciar mayúsculas/minúsculas
        val results = chatRepository.findAllByTitleContainingIgnoreCaseAndIsDeletedFalse("kotlin")

        // Verificación: se debe retornar solo el chat activo que contiene "kotlin" en el título
        assertThat(results)
            .withFailMessage("Se debe retornar únicamente chats activos que contengan 'kotlin' en el título")
            .hasSize(1)

        assertThat(results.first().id).isEqualTo(chat1.id)
        assertThat(results.first().isDeleted).isFalse()

        // Búsqueda adicional que no debería retornar resultados
        val emptyResults = chatRepository.findAllByTitleContainingIgnoreCaseAndIsDeletedFalse("inexistente")
        assertThat(emptyResults).isEmpty()
    }
}

