package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.enums.ChatType
import com.jlaradev.sealtype.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class ChatMemberRepositoryTest @Autowired constructor(
    private val chatMemberRepository: ChatMemberRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User
    private lateinit var chat1: Chat
    private lateinit var chat2: Chat

    @BeforeEach
    fun setUp() {
        // Persistencia de usuarios iniciales para las pruebas
        user1 = userRepository.save(User(username = "user1", passwordHash = "hash1", displayName = "User One"))
        user2 = userRepository.save(User(username = "user2", passwordHash = "hash2", displayName = "User Two"))
        user3 = userRepository.save(User(username = "user3", passwordHash = "hash3", displayName = "User Three"))

        // Persistencia de chats: chat1 se mantiene activo, chat2 se marca como eliminado
        chat1 = chatRepository.save(Chat(type = ChatType.GROUP, title = "Grupo 1", createdBy = user1))
        chat2 = chatRepository.save(Chat(type = ChatType.DIRECT, createdBy = user1, isDeleted = true))

        // Registro de membresías con diversos estados de actividad y relación con chats eliminados
        chatMemberRepository.saveAll(listOf(
            // User 1: Vinculado a un chat activo y a uno eliminado (ambos con membresía activa)
            ChatMember(id = ChatMemberId(chat1.id, user1.id), chat = chat1, user = user1, isAdmin = true, isActive = true),
            ChatMember(id = ChatMemberId(chat2.id, user1.id), chat = chat2, user = user1, isAdmin = true, isActive = true),

            // User 2: Vinculado a un chat activo con membresía activa
            ChatMember(id = ChatMemberId(chat1.id, user2.id), chat = chat1, user = user2, isAdmin = false, isActive = true),

            // User 3: Vinculado a un chat activo pero con membresía inactiva
            ChatMember(id = ChatMemberId(chat1.id, user3.id), chat = chat1, user = user3, isAdmin = false, isActive = false)
        ))
    }

    @Test
    fun testFindAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse() {
        // Ejecución de búsqueda para user1: se debe filtrar el chat eliminado (chat2)
        val resultUser1 = chatMemberRepository.findAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse(user1.id)

        assertThat(resultUser1)
            .withFailMessage("Se debe retornar únicamente el chat activo que no ha sido eliminado")
            .hasSize(1)

        assertThat(resultUser1.first().chat.id).isEqualTo(chat1.id)

        // Ejecución de búsqueda para user3: el resultado debe ser vacío debido a la membresía inactiva
        val resultUser3 = chatMemberRepository.findAllByIdUserIdAndIsActiveTrueAndChatIsDeletedFalse(user3.id)
        assertThat(resultUser3).isEmpty()
    }

    @Test
    fun testFindAllByIdChatIdAndIsActiveTrue() {
        // Obtención de miembros activos pertenecientes al chat1
        val activeMembers = chatMemberRepository.findAllByIdChatIdAndIsActiveTrue(chat1.id)

        // Verificación de integridad: se deben incluir user1 y user2, excluyendo a user3 por inactividad
        assertThat(activeMembers)
            .hasSize(2)
            .extracting("user.id")
            .containsExactlyInAnyOrder(user1.id, user2.id)
    }
}