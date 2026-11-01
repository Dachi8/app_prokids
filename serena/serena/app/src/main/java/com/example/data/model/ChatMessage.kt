package com.example.data.model

/**
 * Mensaje del chat con Serena.
 *
 * El historial vive en memoria durante la sesión; no se guarda en una base
 * local porque la app usa MySQL a través del backend como única fuente de datos.
 */
data class ChatMessageEntity(
    val id: Long = 0,
    val sender: String, // "user" or "serena"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEmergencyAlert: Boolean = false
)

enum class SenderType {
    USER,
    SERENA
}
