package com.example.memoryhelper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,

    secondary = SecondaryTeal,
    onSecondary = Color(0xFF071017),
    secondaryContainer = SecondaryTealDark,
    onSecondaryContainer = Color.White,

    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessGreenDark,
    onTertiaryContainer = Color.White,

    error = ErrorCoral,
    onError = Color.White,
    errorContainer = ErrorCoralDark,
    onErrorContainer = Color.White,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = Color(0xFF3D4048),
    outlineVariant = Color(0xFF2A2C33)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E8FF),
    onPrimaryContainer = Color(0xFF0A2540),

    secondary = SecondaryTeal,
    onSecondary = Color(0xFF003248),
    secondaryContainer = Color(0xFFD4F1FF),
    onSecondaryContainer = Color(0xFF00405C),

    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessGreenContainer,
    onTertiaryContainer = SuccessGreenDark,

    error = ErrorCoral,
    onError = Color.White,
    errorContainer = ErrorCoralContainer,
    onErrorContainer = ErrorCoralDark,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    outline = Color(0xFFD7D9DE),
    outlineVariant = Color(0xFFEEF0F4)
)

@Composable
fun MemoryHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic colors for a consistent branded look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
