package com.example.data.model

/**
 * Tabla `recordatorios` de ProKids.
 * Alertas programadas asociadas a una ejecución de tarea.
 */
data class RecordatorioEntity(
    val id: Long = 0,
    val ejecucionId: Long,
    val horaEnvio: String, // "YYYY-MM-DD HH:mm:ss"
    val enviado: Boolean = false,
    val fechaEnvio: Long? = null
)
