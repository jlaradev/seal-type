package com.jlaradev.sealtype.repository

import com.jlaradev.sealtype.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class UserRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository
) {

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User

    @BeforeEach
    fun setUp() {
        // Persistencia de usuarios con nombres de usuario variados
        user1 = userRepository.save(User(username = "johndoe", passwordHash = "hash1", displayName = "John Doe"))
        user2 = userRepository.save(User(username = "JohnSmith", passwordHash = "hash2", displayName = "John Smith"))
        user3 = userRepository.save(User(username = "marysmith", passwordHash = "hash3", displayName = "Mary Smith"))
    }

    @Test
    fun testFindByUsername() {
        // Búsqueda de usuario existente por nombre de usuario exacto
        val result = userRepository.findByUsername("johndoe")

        assertThat(result)
            .withFailMessage("Se debe retornar el usuario con nombre de usuario 'johndoe'")
            .isNotNull
            .hasFieldOrPropertyWithValue("username", "johndoe")
            .hasFieldOrPropertyWithValue("id", user1.id)

        // Búsqueda de usuario no existente
        val nonExistent = userRepository.findByUsername("inexistente")
        assertThat(nonExistent).isNull()
    }

    @Test
    fun testExistsByUsername() {
        // Verificación de existencia de usuario existente
        val existsJohnDoe = userRepository.existsByUsername("johndoe")
        assertThat(existsJohnDoe)
            .withFailMessage("Debe retornar true para un usuario que existe")
            .isTrue()

        // Verificación de no existencia
        val notExists = userRepository.existsByUsername("inexistente")
        assertThat(notExists)
            .withFailMessage("Debe retornar false para un usuario que no existe")
            .isFalse()
    }

    @Test
    fun testFindAllByUsernameContainingIgnoreCase() {
        // Búsqueda de usuarios que contengan "john" sin diferenciar mayúsculas/minúsculas
        val results = userRepository.findAllByUsernameContainingIgnoreCase("john")

        assertThat(results)
            .withFailMessage("Se deben retornar los usuarios que contengan 'john' en el nombre de usuario")
            .hasSize(2)
            .extracting("username")
            .containsExactlyInAnyOrder("johndoe", "JohnSmith")

        // Búsqueda que retorna un solo resultado
        val singleResult = userRepository.findAllByUsernameContainingIgnoreCase("mary")
        assertThat(singleResult)
            .hasSize(1)
            .extracting("username")
            .containsExactly("marysmith")

        // Búsqueda sin resultados
        val emptyResults = userRepository.findAllByUsernameContainingIgnoreCase("xyz")
        assertThat(emptyResults).isEmpty()
    }
}

