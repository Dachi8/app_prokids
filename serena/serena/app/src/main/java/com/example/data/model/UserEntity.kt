package com.example.data.model

/**
 * Perfil de usuario mapeado a la tabla `usuarios` de MySQL.
 *
 * La app ya no guarda usuarios en Room: este modelo es la representación en
 * memoria del perfil que devuelve el backend ProKids.
 */
data class UserEntity(
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String = "",
    val fechaNacimiento: String? = null,
    val estado: String = "activo",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val ultimoAcceso: Long? = null,
    val rol: String = "PACIENTE",
    val codigoPaciente: String = "",
    val puntosBalance: Int = 0,
    val avatarEmojiStr: String = "🌱"
) {
    // Propiedades de conveniencia usadas por las pantallas de Serena.
    val name: String get() = nombre
    val email: String get() = correo
    val role: String get() = rol
    val patientCode: String get() = codigoPaciente
    val pointsBalance: Int get() = puntosBalance
    val avatarEmoji: String get() = avatarEmojiStr

    constructor(
        id: Long = 0,
        name: String,
        email: String,
        role: String = "PACIENTE",
        patientCode: String = "",
        pointsBalance: Int = 0,
        avatarEmoji: String = "🌱"
    ) : this(
        id = id,
        nombre = name,
        correo = email,
        passwordHash = "",
        fechaNacimiento = null,
        estado = "activo",
        fechaRegistro = System.currentTimeMillis(),
        ultimoAcceso = null,
        rol = role,
        codigoPaciente = patientCode,
        puntosBalance = pointsBalance,
        avatarEmojiStr = avatarEmoji
    )
}

enum class UserRole(val label: String, val badgeEmoji: String, val description: String) {
    PACIENTE("Paciente", "🌱", "Seguimiento personal, chat Serena, libreta y recompensas"),
    MEDICO("Médico / Terapeuta", "🩺", "Supervisión clínica, estadísticas del paciente y asignación de tareas"),
    FAMILIAR("Familiar / Acompañante", "👨‍👩‍👧", "Apoyo cercano, estadísticas y tareas afectivas con recompensa"),
    ADMINISTRADOR("Administrador", "🛡️", "Gestión total de usuarios, roles y asignaciones")
}
