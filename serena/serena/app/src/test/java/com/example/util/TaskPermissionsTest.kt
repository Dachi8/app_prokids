package com.example.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica la regla central de permisos:
 * el paciente NO puede eliminar las tareas de su médico ni de su familia.
 */
class TaskPermissionsTest {

    @Test
    fun `el paciente puede eliminar sus propias metas`() {
        assertTrue(TaskPermissions.canDelete("PACIENTE", "PACIENTE"))
    }

    @Test
    fun `el paciente NO puede eliminar tareas del medico`() {
        assertFalse(TaskPermissions.canDelete("PACIENTE", "MEDICO"))
    }

    @Test
    fun `el paciente NO puede eliminar tareas de la familia`() {
        assertFalse(TaskPermissions.canDelete("PACIENTE", "FAMILIAR"))
    }

    @Test
    fun `el medico solo puede retirar lo que prescribio`() {
        assertTrue(TaskPermissions.canDelete("MEDICO", "MEDICO"))
        assertFalse(TaskPermissions.canDelete("MEDICO", "FAMILIAR"))
        assertFalse(TaskPermissions.canDelete("MEDICO", "PACIENTE"))
    }

    @Test
    fun `la familia solo puede retirar lo que asigno`() {
        assertTrue(TaskPermissions.canDelete("FAMILIAR", "FAMILIAR"))
        assertFalse(TaskPermissions.canDelete("FAMILIAR", "MEDICO"))
        assertFalse(TaskPermissions.canDelete("FAMILIAR", "PACIENTE"))
    }

    @Test
    fun `el administrador puede eliminar cualquier tarea`() {
        assertTrue(TaskPermissions.canDelete("ADMINISTRADOR", "MEDICO"))
        assertTrue(TaskPermissions.canDelete("ADMINISTRADOR", "PACIENTE"))
    }

    @Test
    fun `un rol desconocido no puede eliminar`() {
        assertFalse(TaskPermissions.canDelete("INVITADO", "MEDICO"))
    }
}
