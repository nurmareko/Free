package com.dresta0056.free.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Sage,
    onPrimary = DeepCharcoal,
    primaryContainer = OliveGreen,
    onPrimaryContainer = SoftCream,
    secondary = WarmBeige,
    onSecondary = DeepCharcoal,
    tertiary = MutedBrown,
    background = DeepCharcoal,
    onBackground = SoftCream,
    surface = Color(0xFF23211D),
    onSurface = SoftCream,
    surfaceVariant = Color(0xFF3D3A32),
    onSurfaceVariant = WarmBeige,
    outline = Sage,
    error = Color(0xFFE6A08A)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepOlive,
    onPrimary = CreamSurface,
    primaryContainer = OliveGreen,
    onPrimaryContainer = CreamSurface,
    secondary = Sage,
    onSecondary = DeepCharcoal,
    secondaryContainer = WarmBeige,
    onSecondaryContainer = DeepCharcoal,
    tertiary = MutedBrown,
    onTertiary = CreamSurface,
    background = SoftCream,
    onBackground = DeepCharcoal,
    surface = CreamSurface,
    onSurface = DeepCharcoal,
    surfaceVariant = WarmBeige,
    onSurfaceVariant = Color(0xFF5F614F),
    surfaceContainerLowest = CreamSurface,
    surfaceContainerLow = Color(0xFFFBF6EC),
    surfaceContainer = Color(0xFFF0E8DA),
    surfaceContainerHigh = Color(0xFFE9DECE),
    outline = PaperLine,
    outlineVariant = Color(0xFFE7DCCB),
    error = Color(0xFF9B4F2F)
)

@Composable
fun FreeTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> if (darkTheme) DarkColorScheme else LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
