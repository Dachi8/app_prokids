package com.example.ui.theme

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clasificación del ancho disponible en la pantalla (dp).
 *
 * - [COMPACT]  -> teléfonos (< 600 dp)
 * - [MEDIUM]   -> tablets pequeñas / ventana dividida (600 - 839 dp)
 * - [EXPANDED] -> tablets (840 - 1199 dp)
 * - [ULTRA_WIDE] -> monitores alargados 2400x1080, 2560x1080 (>= 1200 dp)
 */
enum class ClayScreenWidth {
    COMPACT,
    MEDIUM,
    EXPANDED,
    ULTRA_WIDE
}

/**
 * Dimensiones responsivas de Serena.
 *
 * Centraliza el tamaño de botones, iconos, espaciados y tipografías para que
 * los tres perfiles (paciente, médico y familiar) se vean equilibrados tanto
 * en un móvil de 360 dp como en un monitor alargado de 2400x1080.
 */
@Immutable
data class ClayDimens(
    val widthClass: ClayScreenWidth,
    /** Multiplicador global de escala (1.0 = móvil de referencia). */
    val scale: Float,
    /** Ancho máximo del contenido; [Dp.Unspecified] = sin límite. */
    val contentMaxWidth: Dp,
    val screenPadding: Dp,
    val buttonHeight: Dp,
    val buttonHeightSmall: Dp,
    val buttonHeightLarge: Dp,
    val fabSize: Dp,
    val iconTiny: Dp,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val cardRadius: Dp,
    val chipRadius: Dp,
    val gap: Dp,
    val labelSize: TextUnit,
    val bodySize: TextUnit,
    val titleSize: TextUnit,
    /** Tamaño tipográfico para emojis/avatares. */
    val emojiSize: TextUnit,
    /** Tamaño tipográfico para el avatar grande de Configuración. */
    val heroSize: TextUnit
) {
    /** Escala un valor en dp manteniéndolo dentro de un rango legible. */
    fun scaleDp(value: Dp, min: Dp = 0.dp, max: Dp = Dp.Infinity): Dp =
        (value * scale).coerceIn(min, max)

    companion object {
        /** Ancho de referencia: móvil compacto. */
        val COMPACT_MAX_WIDTH_DP = 600
        val MEDIUM_MAX_WIDTH_DP = 840
        val EXPANDED_MAX_WIDTH_DP = 1200

        fun classify(widthDp: Int): ClayScreenWidth = when {
            widthDp < COMPACT_MAX_WIDTH_DP -> ClayScreenWidth.COMPACT
            widthDp < MEDIUM_MAX_WIDTH_DP -> ClayScreenWidth.MEDIUM
            widthDp < EXPANDED_MAX_WIDTH_DP -> ClayScreenWidth.EXPANDED
            else -> ClayScreenWidth.ULTRA_WIDE
        }

        /**
         * Calcula las dimensiones a partir del tamaño de la ventana en dp.
         *
         * @param widthDp ancho disponible en dp
         * @param heightDp alto disponible en dp
         */
        fun of(widthDp: Int, heightDp: Int): ClayDimens {
            val widthClass = classify(widthDp)

            val widthScale = when (widthClass) {
                ClayScreenWidth.COMPACT -> if (widthDp < 340) 0.94f else 1.00f
                ClayScreenWidth.MEDIUM -> 1.06f
                ClayScreenWidth.EXPANDED -> 1.12f
                ClayScreenWidth.ULTRA_WIDE -> 1.20f
            }

            // Pantallas muy bajas (landscape compact,ej. 2400x1080 a densidad alta)
            // reducen un poco la escala para que el contenido siga entrando.
            val heightFactor = when {
                heightDp < 460 -> 0.92f
                heightDp < 560 -> 0.96f
                else -> 1f
            }

            val scale = (widthScale * heightFactor).coerceIn(0.90f, 1.26f)

            val contentMaxWidth = when (widthClass) {
                ClayScreenWidth.COMPACT -> Dp.Unspecified
                ClayScreenWidth.MEDIUM -> 640.dp
                ClayScreenWidth.EXPANDED -> 760.dp
                ClayScreenWidth.ULTRA_WIDE -> 900.dp
            }

            val screenPadding = when (widthClass) {
                ClayScreenWidth.COMPACT -> 20.dp
                ClayScreenWidth.MEDIUM -> 24.dp
                ClayScreenWidth.EXPANDED -> 28.dp
                ClayScreenWidth.ULTRA_WIDE -> 32.dp
            }

            return ClayDimens(
                widthClass = widthClass,
                scale = scale,
                contentMaxWidth = contentMaxWidth,
                screenPadding = screenPadding,
                buttonHeight = (44.dp * scale).coerceIn(44.dp, 58.dp),
                buttonHeightSmall = (36.dp * scale).coerceIn(36.dp, 46.dp),
                buttonHeightLarge = (54.dp * scale).coerceIn(52.dp, 68.dp),
                fabSize = (54.dp * scale).coerceIn(54.dp, 70.dp),
                iconTiny = (14.dp * scale).coerceIn(13.dp, 18.dp),
                iconSmall = (18.dp * scale).coerceIn(16.dp, 23.dp),
                iconMedium = (24.dp * scale).coerceIn(22.dp, 30.dp),
                iconLarge = (32.dp * scale).coerceIn(30.dp, 40.dp),
                cardRadius = (24.dp * scale).coerceIn(22.dp, 30.dp),
                chipRadius = (14.dp * scale).coerceIn(12.dp, 18.dp),
                gap = (12.dp * scale).coerceIn(10.dp, 16.dp),
                labelSize = (13f * scale).coerceIn(12f, 16f).sp,
                bodySize = (15f * scale).coerceIn(14f, 18f).sp,
                titleSize = (19f * scale).coerceIn(18f, 23f).sp,
                emojiSize = (30f * scale).coerceIn(28f, 40f).sp,
                heroSize = (42f * scale).coerceIn(38f, 56f).sp
            )
        }
    }
}

/**
 * Dimensiones responsivas del dispositivo actual.
 * Recalculadas automáticamente cuando cambia la configuración de la ventana.
 */
@Composable
fun rememberClayDimens(): ClayDimens {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        ClayDimens.of(configuration.screenWidthDp, configuration.screenHeightDp)
    }
}

/**
 * Limita el ancho del contenido en pantallas anchas (tablet / monitor alargado)
 * para que las tarjetas no se estiren de formaANTI-ESTÉTICA.
 */
fun Modifier.clayContentWidth(dimens: ClayDimens): Modifier =
    if (dimens.contentMaxWidth == Dp.Unspecified) this else this.widthIn(max = dimens.contentMaxWidth)
