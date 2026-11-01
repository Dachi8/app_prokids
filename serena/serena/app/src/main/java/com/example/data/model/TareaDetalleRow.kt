package com.example.data.model

/**
 * Modelo relacional combinado de una ejecución de tarea con su plantilla,
 * asignación y usuario creador (Médico / Familiar / Administrador).
 */
data class TareaDetalleRow(
    val ejecucionId: Long,
    val asignacionId: Long,
    val tareaId: Long,
    val usuarioId: Long,
    val queHacer: String,
    val descripcion: String?,
    val categoria: String,
    val frecuencia: String,
    val horaLimite: String?,
    val puntosRecompensa: Int,
    val iconoKey: String,
    val fecha: String,
    val estado: String,
    val fechaCompletado: Long?,
    val creadoPorNombre: String,
    val creadoPorRol: String
) {
    val isCompleted: Boolean get() = estado == "completada"
}
