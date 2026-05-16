package com.jlaradev.sealtype.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRequest(
    @field:NotBlank(message = "El nombre de usuario no puede estar vacío")
    @field:Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    val username: String? = null,

    @field:Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    val passwordHash: String? = null,

    @field:NotBlank(message = "El nombre de usuario no puede estar vacío")
    @field:Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    val displayName: String,

    val avatarUrl: String? = null,

    val isOnline: Boolean? = null
)

