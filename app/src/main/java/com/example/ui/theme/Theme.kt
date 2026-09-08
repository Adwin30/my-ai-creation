package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark Schemes
val UchihaCrimsonDarkScheme = darkColorScheme(
    primary = CrimsonGlow,
    onPrimary = Color.White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = SusanooOrange,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF4A2800),
    onSecondaryContainer = SusanooGold,
    background = ObsidianBlack,
    onBackground = TextWhite,
    surface = ObsidianSurface,
    onSurface = TextWhite,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = ObsidianBorder
)

val TsukuyomiDarkScheme = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color.White,
    secondary = TsukuyomiPurple,
    onSecondary = Color.White,
    background = Color(0xFF09080B),
    surface = Color(0xFF141219),
    surfaceVariant = Color(0xFF1F1C26),
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = Color(0xFF322B3D)
)

val SusanooEmberDarkScheme = darkColorScheme(
    primary = SusanooOrange,
    onPrimary = Color.Black,
    secondary = CrimsonGlow,
    onSecondary = Color.White,
    background = Color(0xFF0F0906),
    surface = Color(0xFF1A110B),
    surfaceVariant = Color(0xFF261910),
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = Color(0xFF422A1B)
)

val AmaterasuNoirDarkScheme = darkColorScheme(
    primary = AmaterasuFlame,
    onPrimary = Color.White,
    secondary = CrimsonRed,
    onSecondary = Color.White,
    background = Color(0xFF050505),
    surface = Color(0xFF111111),
    surfaceVariant = Color(0xFF1C1C1C),
    onBackground = TextWhite,
    onSurface = TextWhite,
    outline = Color(0xFF2E2E2E)
)

// Light Schemes
val UchihaCrimsonLightScheme = lightColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    secondary = SusanooOrange,
    onSecondary = Color.White,
    background = Color(0xFFFAF6F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0E8EB),
    onBackground = TextDark,
    onSurface = TextDark,
    outline = Color(0xFFD4C5CA)
)

@Composable
fun ItachiTheme(
    themeName: String = "uchiha_crimson",
    isDarkMode: Boolean = true,
    customPrimaryColor: Color? = null,
    customBgColor: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme: ColorScheme = when {
        isDarkMode -> when (themeName) {
            "tsukuyomi" -> TsukuyomiDarkScheme
            "susanoo" -> SusanooEmberDarkScheme
            "amaterasu" -> AmaterasuNoirDarkScheme
            else -> UchihaCrimsonDarkScheme
        }
        else -> UchihaCrimsonLightScheme
    }

    val finalScheme = if (customPrimaryColor != null || customBgColor != null) {
        baseScheme.copy(
            primary = customPrimaryColor ?: baseScheme.primary,
            background = customBgColor ?: baseScheme.background,
            surface = if (customBgColor != null && isDarkMode) customBgColor.copy(alpha = 0.9f) else baseScheme.surface
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias for default template tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ItachiTheme(
        themeName = "uchiha_crimson",
        isDarkMode = darkTheme,
        content = content
    )
}
