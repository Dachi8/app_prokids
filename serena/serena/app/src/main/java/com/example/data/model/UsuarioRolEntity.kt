package com.example.data.model

/**
 * Tabla `usuarios_roles` de ProKids (vínculo M:N entre usuarios y roles).
 */
data class UsuarioRolEntity(
    val usuarioId: Long,
    val rolId: Long
)
