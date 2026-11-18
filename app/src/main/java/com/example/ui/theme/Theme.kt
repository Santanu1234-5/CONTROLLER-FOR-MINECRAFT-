package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinecraftColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = ObsidianDark,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = DiamondCyan,
    onSecondary = ObsidianDark,
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color.White,
    tertiary = GoldYellow,
    onTertiary = ObsidianDark,
    error = RedstoneRed,
    onError = Color.White,
    background = ObsidianDark,
    onBackground = TextPrimary,
    surface = DeepslateSurface,
    onSurface = TextPrimary,
    surfaceVariant = NetheriteCard,
    onSurfaceVariant = TextSecondary,
    outline = StoneBorder
)

@Composable
fun MinecraftControllerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinecraftColorScheme,
        typography = Typography,
        content = content
    )
}
