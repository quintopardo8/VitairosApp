package com.vitairos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VitairosTealPrimary,
    onPrimary = VitairosDarkBackground,
    secondary = VitairosTealSecondary,
    onSecondary = VitairosDarkBackground,
    tertiary = VitairosTealLight,
    background = VitairosDarkBackground,
    onBackground = VitairosTextPrimary,
    surface = VitairosSurfaceDark,
    onSurface = VitairosTextPrimary,
    surfaceVariant = VitairosSurfaceVariant,
    onSurfaceVariant = VitairosTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = VitairosTealPrimary,
    onPrimary = VitairosTextPrimary,
    secondary = VitairosTealSecondary,
    onSecondary = VitairosTextPrimary,
    background = VitairosDarkBackground,
    surface = VitairosSurfaceDark,
    onSurface = VitairosTextPrimary
)

@Composable
fun VitairosAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Paleta oscura -> VITAIROS
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}