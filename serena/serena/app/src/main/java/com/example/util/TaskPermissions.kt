package com.example.util

/**
 * Reglas de permisos sobre las tareas (metas) de Serena.
 *
 * Regla principal: **el paciente NO puede eliminar las tareas asignadas por su
 * médico ni por su familia**. Solo puede gestionar las metas que él mismo creó.
 */
object TaskPermissions {

    const val ROLE_PATIENT = "PACIENTE"
    const val ROLE_DOCTOR = "MEDICO"
    const val ROLE_FAMILY = "FAMILIAR"
    const val ROLE_ADMIN = "ADMINISTRADOR"

    /**
     * @param callerRole rol del perfil que intenta eliminar la tarea.
     * @param assignedByRole rol que asignó la tarea.
     */
    fun canDelete(callerRole: String, assignedByRole: String): Boolean = when (callerRole) {
        ROLE_PATIENT -> assignedByRole == ROLE_PATIENT
        ROLE_DOCTOR -> assignedByRole == ROLE_DOCTOR
        ROLE_FAMILY -> assignedByRole == ROLE_FAMILY
        ROLE_ADMIN -> true
        else -> false
    }

    /** Mensaje mostrado cuando el permiso es denegado. */
    fun denialMessage(callerRole: String): String = when (callerRole) {
        ROLE_PATIENT -> "Solo puedes eliminar tus propias metas. Las tareas de tu médico o tu familia las eliminan ellos."
        ROLE_DOCTOR -> "Solo puedes retirar las tareas que tú prescribiste."
        ROLE_FAMILY -> "Solo puedes retirar las tareas que tú asignaste."
        else -> "No tienes permiso para eliminar esta tarea."
    }
}
