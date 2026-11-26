package com.globalcampus.support.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = HighlightSkyBlue,
    onPrimaryContainer = PrimaryNavy,
    secondary = SecondaryRoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = HighlightSkyBlue,
    onSecondaryContainer = SecondaryRoyalBlue,
    tertiary = AccentTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF0F766E),
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = SecondaryRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = AccentTeal,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = AccentTeal,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF24334A),
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun GlobalCampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    // Tint the system status bar to match the theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
