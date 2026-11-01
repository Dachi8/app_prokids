package com.example.data.model

/**
 * Tarea prescrita a un paciente. El `id` es el de la asignación en MySQL
 * (`tarea_asignaciones.id`), que es lo que el servidor usa para completarla
 * o eliminarla.
 */
data class PrescribedTaskEntity(
    val id: Long = 0,
    val title: String,
    val description: String,
    val assignedByRole: String, // "MEDICO", "FAMILIAR" o "PACIENTE"
    val assignedByName: String,
    val pointsReward: Int = 50,
    val category: String = "Autocuidado",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val pacienteId: Long = 1
)
