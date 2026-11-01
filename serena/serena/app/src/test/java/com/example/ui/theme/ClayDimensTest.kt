package com.example.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica el sistema de dimensiones responsivas.
 *
 * Objetivo: que los botones se vean bien tanto en un móvil de 360 dp como
 * en un monitor alargado de 2400x1080 (o 1920x1080).
 */
class ClayDimensTest {

    @Test
    fun `un movil compacto usa la escala de referencia`() {
        val dimens = ClayDimens.of(widthDp = 360, heightDp = 800)
        assertEquals(ClayScreenWidth.COMPACT, dimens.widthClass)
        assertEquals(1.00f, dimens.scale, 0.001f)
        assertEquals(44f, dimens.buttonHeight.value, 0.01f)
    }

    @Test
    fun `una tablet amplia escala los botones`() {
        val compact = ClayDimens.of(360, 800)
        val tablet = ClayDimens.of(1024, 800)
        assertEquals(ClayScreenWidth.EXPANDED, tablet.widthClass)
        assertTrue(tablet.buttonHeight > compact.buttonHeight)
        assertTrue(tablet.iconMedium > compact.iconMedium)
    }

    @Test
    fun `un monitor 1920x1080 se clasifica como ULTRA WIDE`() {
        val dimens = ClayDimens.of(1920, 1080)
        assertEquals(ClayScreenWidth.ULTRA_WIDE, dimens.widthClass)
        assertTrue(dimens.contentMaxWidth.value < 1920f)
    }

    @Test
    fun `un monitor alargado 2400x1080 se clasifica como ULTRA WIDE`() {
        val dimens = ClayDimens.of(2400, 1080)
        assertEquals(ClayScreenWidth.ULTRA_WIDE, dimens.widthClass)
        // El contenido se acota para que las tarjetas no se estiren
        assertTrue(dimens.contentMaxWidth.value > 0f)
        assertTrue(dimens.contentMaxWidth.value < 2400f)
    }

    @Test
    fun `un monitor 1920x1080 tambien queda acotado`() {
        val dimens = ClayDimens.of(1920, 1080)
        assertEquals(ClayScreenWidth.ULTRA_WIDE, dimens.widthClass)
        assertTrue(dimens.contentMaxWidth.value < 1920f)
    }

    @Test
    fun `los botones mantienen un area tactil minima de 44dp`() {
        listOf(320, 360, 480, 600, 840, 1200, 1920, 2400, 3440).forEach { width ->
            val dimens = ClayDimens.of(width, 1080)
            assertTrue(
                "Ancho $width produce botones de ${dimens.buttonHeight}",
                dimens.buttonHeight.value >= 44f
            )
        }
    }

    @Test
    fun `una pantalla muy baja reduce la escala`() {
        val normal = ClayDimens.of(360, 800)
        val baja = ClayDimens.of(360, 400)
        assertTrue(baja.scale < normal.scale)
    }

    @Test
    fun `los botones nunca superan el limite superior`() {
        val ultra = ClayDimens.of(3440, 1440)
        assertTrue(ultra.buttonHeight.value <= 58f)
        assertTrue(ultra.fabSize.value <= 70f)
    }
}
