package com.example.data.model

/**
 * Tabla `tarea_asignaciones` de ProKids.
 * Nivel 2 - La ASIGNACIÓN: a qué paciente le corresponde esta tarea plantilla.
 */
data class TareaAsignacionEntity(
    val id: Long = 0,
    val tareaId: Long,
    val usuarioId: Long,
    val fechaRegistro: Long = System.currentTimeMillis()
)
