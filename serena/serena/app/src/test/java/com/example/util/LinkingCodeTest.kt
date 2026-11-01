package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del generador de códigos de vinculación.
 */
class LinkingCodeTest {

    @Test
    fun `el codigo tiene el formato SERENA seguido de 4 digitos`() {
        repeat(200) {
            val code = LinkingCode.generate()
            assertTrue("Formato inválido: $code", code.matches(Regex("SERENA-\\d{4}")))
        }
    }

    @Test
    fun `los codigos generados son distintos entre si`() {
        // Con 8 dígitos hay 100.000.000 de combinaciones: 500 muestras no
        // deberían repetir ni un solo código.
        val codes = (1..500).map { LinkingCode.generate(8) }.toSet()
        assertEquals(500, codes.size)
    }

    @Test
    fun `un codigo nunca coincide con otro`() {
        val first = LinkingCode.generate()
        val second = LinkingCode.generate()
        assertNotEquals(first, second)
    }

    @Test
    fun `normalize limpia espacios y mayusculas`() {
        assertEquals("SERENA-1234", LinkingCode.normalize("  serena-1234 "))
        assertEquals("SERENA-1234", LinkingCode.normalize("serena 1234"))
        assertEquals("SERENA-1234", LinkingCode.normalize("SERENA1234"))
        assertEquals("SERENA-1234", LinkingCode.normalize("serena_1234"))
    }

    @Test
    fun `normalize conserva el prefijo cuando ya viene completo`() {
        assertEquals("SERENA-0007", LinkingCode.normalize("SERENA-0007"))
    }
}
