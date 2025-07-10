package com.amazon.ivs.screensharing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BlackColorPrimary,
    secondary = BlackColorPrimary,
    tertiary = BlackColorPrimary,
    background = BlackColorPrimary,
    surface = BlackColorPrimary,
    error = RedColorPrimaryDark,
    onPrimary = WhiteColorPrimary,
    onSecondary = WhiteColorPrimary,
    onTertiary = WhiteColorPrimary,
    onBackground = WhiteColorPrimary,
    onSurface = WhiteColorPrimary,
    onError = WhiteColorPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = WhiteColorPrimary,
    secondary = WhiteColorPrimary,
    tertiary = WhiteColorPrimary,
    background = WhiteColorPrimary,
    surface = WhiteColorPrimary,
    error = RedColorPrimaryLight,
    onPrimary = BlackColorPrimary,
    onSecondary = BlackColorPrimary,
    onTertiary = BlackColorPrimary,
    onBackground = BlackColorPrimary,
    onSurface = BlackColorPrimary,
    onError = WhiteColorPrimary,
)

@Composable
fun ScreenSharingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
