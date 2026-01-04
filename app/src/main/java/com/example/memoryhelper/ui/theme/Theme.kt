package com.example.memoryhelper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,

    secondary = SecondaryTealLight,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryTealDark,
    onSecondaryContainer = Color.White,

    tertiary = SuccessGreenLight,
    onTertiary = Color.Black,
    tertiaryContainer = SuccessGreenDark,
    onTertiaryContainer = Color.White,

    error = ErrorCoralLight,
    onError = Color.Black,
    errorContainer = ErrorCoralDark,
    onErrorContainer = Color.White,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = Color(0xFF5F6368),
    outlineVariant = Color(0xFF3C4043)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = PrimaryBlueDark,

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = SecondaryTealDark,

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

    outline = Color(0xFFDADCE0),
    outlineVariant = Color(0xFFE8EAED)
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
        content = content
    )
}
