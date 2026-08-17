package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.theme.ThemeDefinition

@Composable
fun ModuxTheme(
    themeDefinition: ThemeDefinition,
    accentOverride: String? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val primaryColor = if (accentOverride != null) {
        try {
            val clean = accentOverride.removePrefix("#")
            Color((0xFF000000 or clean.toLong(16)).toInt())
        } catch (_: Exception) {
            themeDefinition.toAccentColor()
        }
    } else {
        themeDefinition.toAccentColor()
    }

    val colorScheme: ColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (themeDefinition.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (themeDefinition.isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = if (isLightColor(primaryColor)) Color(0xFF0F172A) else Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = themeDefinition.toBackgroundColor(),
            onBackground = themeDefinition.toTextPrimaryColor(),
            surface = themeDefinition.toSurfaceColor(),
            onSurface = themeDefinition.toTextPrimaryColor(),
            surfaceVariant = themeDefinition.toCardColor(),
            onSurfaceVariant = themeDefinition.toTextSecondaryColor(),
            outline = themeDefinition.toBorderColor()
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            background = themeDefinition.toBackgroundColor(),
            onBackground = themeDefinition.toTextPrimaryColor(),
            surface = themeDefinition.toSurfaceColor(),
            onSurface = themeDefinition.toTextPrimaryColor(),
            surfaceVariant = themeDefinition.toCardColor(),
            onSurfaceVariant = themeDefinition.toTextSecondaryColor(),
            outline = themeDefinition.toBorderColor()
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun isLightColor(color: Color): Boolean {
    val luminance = (0.299f * color.red + 0.587f * color.green + 0.114f * color.blue)
    return luminance > 0.5f
}
