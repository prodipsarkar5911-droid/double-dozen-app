package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CasinoColorScheme = darkColorScheme(
  primary = CasinoGold,
  onPrimary = Color(0xFF1E1500),
  primaryContainer = Color(0xFF3D2F05),
  onPrimaryContainer = CasinoGoldGlow,
  secondary = NeonEmerald,
  onSecondary = Color(0xFF00220E),
  secondaryContainer = Color(0xFF003819),
  onSecondaryContainer = NeonEmeraldGlow,
  tertiary = RouletteRed,
  onTertiary = Color.White,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DarkCardBorder,
  error = RouletteRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  // Always use rich dark casino palette for optimal readability, high contrast and casino vibe
  MaterialTheme(
    colorScheme = CasinoColorScheme,
    typography = Typography,
    content = content
  )
}
