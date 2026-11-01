package com.example.data.model

/**
 * Tabla `tarea_ejecuciones` de ProKids.
 * Nivel 3 - La EJECUCIÓN: una fila por cada día o fecha en que se debía cumplir la tarea.
 */
data class TareaEjecucionEntity(
    val id: Long = 0,
    val asignacionId: Long,
    val fecha: String, // YYYY-MM-DD
    val estado: String = "pendiente",
    val fechaCompletado: Long? = null
)
