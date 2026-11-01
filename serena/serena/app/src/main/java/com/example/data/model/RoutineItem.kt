package com.example.data.model

/**
 * Rutina de bienestar, mapeada a la tabla `rutinas` de MySQL.
 */
data class RoutineEntity(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String,
    val iconKey: String = "heart",
    val completedDates: String = "",
    val streak: Int = 0,
    val lastCompletedDate: String = "",
    val targetDaysPerWeek: Int = 7,
    val usuarioId: Long = 1
) {
    fun isCompletedOn(dateString: String): Boolean {
        return completedDates.split(",").map { it.trim() }.contains(dateString)
    }
}

enum class RoutineCategory(val label: String, val iconEmoji: String) {
    MORNING("Mañana", "🌅"),
    WELLNESS("Bienestar", "🌿"),
    NIGHT("Noche", "🌙")
}
