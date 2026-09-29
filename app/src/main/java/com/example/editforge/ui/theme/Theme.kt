package com.example.editforge.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun EditForgeTheme(
    palette: StudioThemePalette = StudioThemePalette.CYBER_FORGE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val studioColors = palette.toStudioColors()

    val colorScheme = darkColorScheme(
        primary = studioColors.primary,
        onPrimary = studioColors.onPrimary,
        primaryContainer = studioColors.primaryContainer,
        onPrimaryContainer = studioColors.onPrimaryContainer,

        secondary = studioColors.secondary,
        onSecondary = studioColors.onSecondary,
        secondaryContainer = studioColors.secondaryContainer,
        onSecondaryContainer = studioColors.onSecondaryContainer,

        tertiary = studioColors.tertiary,
        onTertiary = studioColors.background,
        tertiaryContainer = studioColors.surfaceElevated,
        onTertiaryContainer = studioColors.secondary,

        background = studioColors.background,
        onBackground = studioColors.textPrimary,

        surface = studioColors.surface,
        onSurface = studioColors.textPrimary,
        surfaceVariant = studioColors.surfaceElevated,
        onSurfaceVariant = studioColors.textSecondary,

        outline = studioColors.border,
        outlineVariant = studioColors.border
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = studioColors.background.toArgb()
            window.navigationBarColor = studioColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalStudioColors provides studioColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
