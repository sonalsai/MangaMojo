package com.mangamojo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.mangamojo.app.domain.model.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = VioletLight,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = VioletDark,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
)

/**
 * App theme driven by the user's [ThemeMode] preference. We deliberately use a
 * fixed brand palette rather than dynamic color so the reader looks consistent
 * across devices.
 */
@Composable
fun MangaMojoTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
