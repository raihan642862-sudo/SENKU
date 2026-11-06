package com.headshotaipro.trainer.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkGamingColorScheme = darkColorScheme(
    primary = CrimsonHeadshot,
    onPrimary = TextPrimary,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyberCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = CyberCyanDim,
    onSecondaryContainer = TextPrimary,
    tertiary = TacticalGold,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalElevated
)

@Composable
fun HeadshotAIProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkGamingColorScheme,
        typography = HeadshotTypography,
        content = content
    )
}
