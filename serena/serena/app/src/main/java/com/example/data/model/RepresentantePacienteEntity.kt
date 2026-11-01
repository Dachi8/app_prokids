package com.example.data.model

/**
 * Tabla `representantes_pacientes` de ProKids.
 * Vincula a un Representante (Familiar, Tutor, Médico) con un Paciente.
 */
data class RepresentantePacienteEntity(
    val id: Long = 0,
    val representanteId: Long,
    val pacienteId: Long,
    val parentesco: String? = "Familiar",
    val fechaRegistro: Long = System.currentTimeMillis()
)
