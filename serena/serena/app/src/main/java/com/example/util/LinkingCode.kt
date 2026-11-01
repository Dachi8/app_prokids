package com.example.util

import java.security.SecureRandom

/**
 * Generador de códigos de vinculación de Serena.
 *
 * Cada perfil (paciente, médico o familiar) recibe un código único con el
 * formato `SERENA-XXXX`. El repositorio valida la unicidad contra la base de
 * datos antes de persistirlo, por lo que nunca existen dos usuarios con el
 * mismo código.
 */
object LinkingCode {

    const val PREFIX = "SERENA"
    const val SEPARATOR = "-"
    const val LENGTH = 4

    private const val DIGITS = "0123456789"
    private val secureRandom = SecureRandom()

    /** Genera un código aleatorio con el formato `SERENA-XXXX`. */
    fun generate(length: Int = LENGTH): String {
        val suffix = buildString {
            repeat(length) {
                append(DIGITS[secureRandom.nextInt(DIGITS.length)])
            }
        }
        return "$PREFIX$SEPARATOR$suffix"
    }

    /**
     * Normaliza lo que el usuario escribe para poder compararlo con el código
     * almacenado (mayúsculas, espacios y guiones sobrantes).
     */
    fun normalize(raw: String): String {
        val cleaned = raw.trim().uppercase()
            .replace(" ", "")
            .replace("_", SEPARATOR)
        return if (cleaned.startsWith("$PREFIX$SEPARATOR")) {
            cleaned
        } else if (cleaned.startsWith(PREFIX)) {
            "$PREFIX$SEPARATOR${cleaned.removePrefix(PREFIX).trimStart('-', ' ')}"
        } else {
            cleaned
        }
    }
}
