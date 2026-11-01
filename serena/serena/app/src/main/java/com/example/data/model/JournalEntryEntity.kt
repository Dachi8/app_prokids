package com.example.data.model

/**
 * Entrada de la Libreta de Sentimientos, mapeada a la tabla `notas` de MySQL.
 */
data class JournalEntryEntity(
    val id: Long = 0,
    val userId: Long = 1,
    val title: String,
    val content: String,
    val emotion: String = "Calma",
    val emotionEmoji: String = "💭",
    val intensity: Int = 3,
    val isSharedWithCareTeam: Boolean = true,
    val reminderTimestamp: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = ""
)
