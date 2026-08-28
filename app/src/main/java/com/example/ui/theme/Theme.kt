package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class GameThemeColors(
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val coinGold: Color,
    val success: Color,
    val error: Color
)

val CyberColors = GameThemeColors(
    background = CyberDarkBg,
    surface = CyberSurface,
    cardBg = CyberCard,
    cardBorder = CyberCardBorder,
    primary = NeonCyan,
    secondary = NeonPurple,
    accent = NeonPink,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    coinGold = CoinGold,
    success = SuccessGreen,
    error = ErrorRed
)

val NebulaColors = GameThemeColors(
    background = NebulaIndigo,
    surface = NebulaSurface,
    cardBg = NebulaCard,
    cardBorder = Color(0xFF43308F),
    primary = NebulaTeal,
    secondary = NebulaMagenta,
    accent = Color(0xFFB388FF),
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    coinGold = CoinGold,
    success = SuccessGreen,
    error = ErrorRed
)

val GoldColors = GameThemeColors(
    background = GoldDarkBg,
    surface = GoldSurface,
    cardBg = GoldCard,
    cardBorder = Color(0xFF5E4E2C),
    primary = GoldPrimary,
    secondary = GoldAccent,
    accent = Color(0xFFFFD54F),
    textPrimary = Color(0xFFFFF8E1),
    textSecondary = Color(0xFFD7CCC8),
    textMuted = Color(0xFF8D6E63),
    coinGold = CoinGold,
    success = SuccessGreen,
    error = ErrorRed
)

val ZenColors = GameThemeColors(
    background = ZenDarkBg,
    surface = ZenSurface,
    cardBg = ZenCard,
    cardBorder = Color(0xFF2A5942),
    primary = ZenGreen,
    secondary = ZenMint,
    accent = Color(0xFFB9F6CA),
    textPrimary = TextPrimary,
    textSecondary = Color(0xFFA7C7B5),
    textMuted = Color(0xFF5C7A6B),
    coinGold = CoinGold,
    success = SuccessGreen,
    error = ErrorRed
)

val ArcadeColors = GameThemeColors(
    background = ArcadeDarkBg,
    surface = ArcadeSurface,
    cardBg = ArcadeCard,
    cardBorder = Color(0xFF6529A3),
    primary = ArcadeHotPink,
    secondary = ArcadeOrange,
    accent = ArcadeYellow,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    coinGold = CoinGold,
    success = SuccessGreen,
    error = ErrorRed
)

val LocalGameColors = staticCompositionLocalOf { CyberColors }

@Composable
fun BrainAppTheme(
    themeKey: String = "THEME_CYBER",
    content: @Composable () -> Unit
) {
    val gameColors = when (themeKey) {
        "THEME_NEBULA" -> NebulaColors
        "THEME_GOLD" -> GoldColors
        "THEME_ZEN" -> ZenColors
        "THEME_ARCADE" -> ArcadeColors
        else -> CyberColors
    }

    val m3ColorScheme = darkColorScheme(
        primary = gameColors.primary,
        secondary = gameColors.secondary,
        tertiary = gameColors.accent,
        background = gameColors.background,
        surface = gameColors.surface,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = gameColors.textPrimary,
        onSurface = gameColors.textPrimary,
        surfaceVariant = gameColors.cardBg,
        onSurfaceVariant = gameColors.textSecondary,
        outline = gameColors.cardBorder
    )

    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
