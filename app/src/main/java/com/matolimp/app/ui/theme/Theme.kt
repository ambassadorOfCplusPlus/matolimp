package com.matolimp.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Blue = Color(0xFF2E5BFF)
private val BlueDark = Color(0xFF8AA4FF)

private val LightColors = lightColorScheme(
    primary = Blue,
    secondary = Color(0xFF00A36C)
)

private val DarkColors = darkColorScheme(
    primary = BlueDark,
    secondary = Color(0xFF57D9A3)
)

@Composable
fun MatOlimpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
