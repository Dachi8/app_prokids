package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPurplePrimary,
    primaryContainer = DarkPurpleContainer,
    onPrimary = Color(0xFF381E72),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    tertiaryContainer = Color(0xFF633B48),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimary = Color.White,
    onPrimaryContainer = OnPurplePrimaryContainer,
    secondary = LavenderSecondary,
    secondaryContainer = LavenderSecondaryContainer,
    onSecondary = Color.White,
    onSecondaryContainer = OnLavenderSecondaryContainer,
    tertiary = OrchidTertiary,
    tertiaryContainer = OrchidTertiaryContainer,
    onTertiary = Color.White,
    onTertiaryContainer = OnOrchidTertiaryContainer,
    background = WhiteBackground,
    surface = WhiteSurface,
    surfaceVariant = SurfaceVariantLilac,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryMuted,
    outline = Color(0xFFDDD2ED)
)

@Composable
fun SerenaTheme(
    darkTheme: Boolean = false, // Mantener paleta pastel y superficies blancas nítidas para máxima legibilidad
    dynamicColor: Boolean = false, // Mantener consistente la paleta de bienestar Serena
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = SerenaTheme(darkTheme, dynamicColor, content)
