package com.example.data.model

/**
 * Tabla `tareas` de ProKids.
 * Nivel 1 - La PLANTILLA: qué es la tarea, quién la creó y cada cuánto se repite.
 */
data class TareaEntity(
    val id: Long = 0,
    val creadoPor: Long,
    val queHacer: String,
    val descripcion: String? = null,
    val frecuencia: String = "diaria",
    val horaLimite: String? = null,
    val categoria: String = "Autocuidado",
    val puntosRecompensa: Int = 50,
    val iconoKey: String = "heart",
    val fechaRegistro: Long = System.currentTimeMillis()
)
