package com.weeker.app.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun WeekerTheme(theme: AppThemeConfig, content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = theme.primary,
        onPrimary = theme.onPrimary,
        background = theme.background,
        onBackground = theme.onBackground,
        surface = theme.surface,
        onSurface = theme.onSurface,
        secondary = theme.primary,
        onSecondary = theme.onPrimary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

fun AppThemeConfig.asColorScheme(): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    secondary = primary,
    onSecondary = onPrimary
)
