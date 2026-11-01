package com.example.data.model

/**
 * Tabla `roles` de ProKids (IAM / RBAC).
 */
data class RolEntity(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String? = null
)
