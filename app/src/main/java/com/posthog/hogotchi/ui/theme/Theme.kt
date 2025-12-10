package com.posthog.hogotchi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PostHogOrange = Color(0xFFf54e00)
val PostHogYellow = Color(0xFFffeb3b)
val PostHogDark = Color(0xFF1d1f27)
val PostHogDarkSurface = Color(0xFF2c2e38)
val PostHogLightGray = Color(0xFFe5e7eb)

private val DarkColorScheme = darkColorScheme(
    primary = PostHogOrange,
    secondary = PostHogYellow,
    tertiary = PostHogOrange,
    background = PostHogDark,
    surface = PostHogDarkSurface,
    onPrimary = Color.White,
    onSecondary = PostHogDark,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PostHogOrange,
    secondary = PostHogYellow,
    tertiary = PostHogOrange,
    background = Color.White,
    surface = PostHogLightGray,
    onPrimary = Color.White,
    onSecondary = PostHogDark,
    onTertiary = Color.White,
    onBackground = PostHogDark,
    onSurface = PostHogDark
)

@Composable
fun HogotchiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
