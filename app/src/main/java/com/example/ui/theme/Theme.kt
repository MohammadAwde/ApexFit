package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BentoPrimary,
    onPrimary = BentoPrimaryDark,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoPrimaryHighlight,
    secondary = BentoPrimaryHighlight,
    onSecondary = BentoPrimaryDeep,
    secondaryContainer = BentoSurfaceVariant,
    onSecondaryContainer = BentoPrimary,
    tertiary = GoldStar,
    onTertiary = BentoPrimaryDeep,
    background = BentoBackground,
    onBackground = BentoTextPrimary,
    surface = BentoSurface,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoSurfaceVariant,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoOutline,
    outlineVariant = BentoSurfaceVariant,
    error = BentoHeartRateRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BentoLightPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoLightPrimaryContainer,
    onPrimaryContainer = BentoPrimaryDeep,
    secondary = BentoPrimaryContainer,
    onSecondary = Color.White,
    secondaryContainer = BentoLightSurfaceVariant,
    onSecondaryContainer = BentoLightPrimary,
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    background = BentoLightBackground,
    onBackground = LightTextPrimary,
    surface = BentoLightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = BentoLightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = BentoLightOutline,
    outlineVariant = Color(0xFFCAC4D0),
    error = BentoHeartRateRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
