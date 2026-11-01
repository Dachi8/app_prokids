package com.example.data.model

/**
 * Tabla `dispositivos_push` de ProKids.
 * Guarda tokens FCM para el envío automático de notificaciones push de tareas.
 */
data class DispositivoPushEntity(
    val id: Long = 0,
    val usuarioId: Long,
    val fcmToken: String,
    val plataforma: String = "android",
    val fechaRegistro: Long = System.currentTimeMillis()
)
