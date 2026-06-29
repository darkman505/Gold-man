package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    tertiary = AmberAccent,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = DarkBg,
    onSecondary = LightText,
    onBackground = LightText,
    onSurface = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    tertiary = AmberAccent,
    background = android.graphics.Color.parseColor("#FDFBF7").let { androidx.compose.ui.graphics.Color(it) },
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = DarkBg,
    onSecondary = DarkBg,
    onBackground = DarkBg,
    onSurface = DarkBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Gold aesthetic by default for luxury look
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

