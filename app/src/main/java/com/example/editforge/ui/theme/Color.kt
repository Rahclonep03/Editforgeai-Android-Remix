package com.example.editforge.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// Dynamic Studio Theme Color Hooks
// Every reference to StudioDarkBackground, ForgeNeonRed, StudioBorder, etc.
// automatically resolves to the currently active StudioThemePalette!
// =========================================================================

val StudioDarkBackground: Color
    @Composable
    get() = StudioTheme.colors.background

val StudioCardSurface: Color
    @Composable
    get() = StudioTheme.colors.surface

val StudioCardSurfaceElevated: Color
    @Composable
    get() = StudioTheme.colors.surfaceElevated

val StudioBorder: Color
    @Composable
    get() = StudioTheme.colors.border

val ForgeNeonRed: Color
    @Composable
    get() = StudioTheme.colors.primary

val ForgeCrimson: Color
    @Composable
    get() = StudioTheme.colors.primary

val ForgeCrimsonDark: Color
    @Composable
    get() = StudioTheme.colors.primaryContainer

val ForgeElectricAmber: Color
    @Composable
    get() = StudioTheme.colors.secondary

val ForgeGoldDark: Color
    @Composable
    get() = StudioTheme.colors.secondaryContainer

val ForgeWaveformViolet: Color
    @Composable
    get() = StudioTheme.colors.waveformColor

val ForgeWaveformDim: Color
    @Composable
    get() = StudioTheme.colors.border

val ForgeWaveformCyan: Color
    @Composable
    get() = StudioTheme.colors.tertiary

val TextPrimary: Color
    @Composable
    get() = StudioTheme.colors.textPrimary

val TextSecondary: Color
    @Composable
    get() = StudioTheme.colors.textSecondary

val TextTertiary: Color
    @Composable
    get() = StudioTheme.colors.textTertiary

// System status indicators
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusProcessing = Color(0xFF38BDF8)
val StatusFailed = Color(0xFFEF4444)

/**
 * Studio dynamic theme colors that change according to the user's selected palette in Settings
 */
data class StudioColors(
    val primary: Color,
    val onPrimary: Color = Color.White,
    val primaryContainer: Color,
    val onPrimaryContainer: Color = Color(0xFFF8FAFC),
    val secondary: Color,
    val onSecondary: Color = Color.White,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color = Color(0xFFF8FAFC),
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val accentGlow: Color,
    val waveformColor: Color,
    val textPrimary: Color = Color(0xFFF8FAFC),
    val textSecondary: Color = Color(0xFF94A3B8),
    val textTertiary: Color = Color(0xFF64748B)
)

/**
 * The branding colour palettes derived from the EditForge AI logos:
 * 1. CYBER_FORGE (Silver/Gold Nodes + Coral Red Anvil + Obsidian Black)
 * 2. ELECTRIC_AZURE (Sky Blue 3D Network + Amber Gold Anvil + Matte Navy)
 * 3. SYNTHWAVE_NEON (Aqua Cyan Glow + Hot Magenta Anvil + Cosmic Void)
 * 4. ARTISAN_BRONZE (Burnished Gold Anvil + Textured Patina Teal Network on Matte Charcoal)
 * 5. SOLAR_AMBER (Sunburst Amber Gold + Molten Crimson Anvil + Deep Onyx)
 */
enum class StudioThemePalette(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val cardColor: Color,
    val accentGlow: Color
) {
    CYBER_FORGE(
        id = "cyber_forge",
        title = "Cyber Forge",
        subtitle = "Silver & Gold Nodes with Glowing Crimson Anvil on Obsidian",
        badgeLabel = "Obsidian Core",
        primaryColor = Color(0xFFFF3B47),
        secondaryColor = Color(0xFFFFB800),
        backgroundColor = Color(0xFF0D0F17),
        cardColor = Color(0xFF151824),
        accentGlow = Color(0xFFFF3B47)
    ),
    ELECTRIC_AZURE(
        id = "electric_azure",
        title = "Electric Azure",
        subtitle = "3D Sky Blue Network with Golden Amber Anvil on Slate Navy",
        badgeLabel = "Oceanic Depth",
        primaryColor = Color(0xFF00B4D8),
        secondaryColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF0B192C),
        cardColor = Color(0xFF132B45),
        accentGlow = Color(0xFF00B4D8)
    ),
    SYNTHWAVE_NEON(
        id = "synthwave_neon",
        title = "Synthwave Neon",
        subtitle = "Electric Aqua Glow with Hot Magenta Anvil on Cosmic Void",
        badgeLabel = "Cyberpunk Glow",
        primaryColor = Color(0xFF00F5D4),
        secondaryColor = Color(0xFFFF007F),
        backgroundColor = Color(0xFF0B0914),
        cardColor = Color(0xFF171329),
        accentGlow = Color(0xFFFF007F)
    ),
    ARTISAN_BRONZE(
        id = "artisan_bronze",
        title = "Artisan Bronze",
        subtitle = "Burnished Gold Anvil with Textured Patina Teal Network on Matte Charcoal",
        badgeLabel = "Patina Forge",
        primaryColor = Color(0xFF2E8B9A),
        secondaryColor = Color(0xFFD4A348),
        backgroundColor = Color(0xFF111317),
        cardColor = Color(0xFF181B22),
        accentGlow = Color(0xFFD4A348)
    ),
    SOLAR_AMBER(
        id = "solar_amber",
        title = "Solar Amber",
        subtitle = "Sunburst Amber Gold with Molten Crimson Forge on Deep Onyx",
        badgeLabel = "Molten Core",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFEF4444),
        backgroundColor = Color(0xFF12100E),
        cardColor = Color(0xFF1E1A17),
        accentGlow = Color(0xFFF59E0B)
    );

    fun toStudioColors(): StudioColors {
        return when (this) {
            CYBER_FORGE -> StudioColors(
                primary = Color(0xFFFF3B47),
                primaryContainer = Color(0xFF4A1017),
                secondary = Color(0xFFFFB800),
                secondaryContainer = Color(0xFF382607),
                tertiary = Color(0xFFD1D5DB),
                background = Color(0xFF0D0F17),
                surface = Color(0xFF151824),
                surfaceElevated = Color(0xFF1E2335),
                border = Color(0xFF282F45),
                accentGlow = Color(0xFFFF3B47),
                waveformColor = Color(0xFFFF3B47)
            )
            ELECTRIC_AZURE -> StudioColors(
                primary = Color(0xFF00B4D8),
                primaryContainer = Color(0xFF093753),
                secondary = Color(0xFFF59E0B),
                secondaryContainer = Color(0xFF422808),
                tertiary = Color(0xFF90E0EF),
                background = Color(0xFF0B192C),
                surface = Color(0xFF132B45),
                surfaceElevated = Color(0xFF1E3E62),
                border = Color(0xFF244A75),
                accentGlow = Color(0xFF00B4D8),
                waveformColor = Color(0xFF00B4D8)
            )
            SYNTHWAVE_NEON -> StudioColors(
                primary = Color(0xFF00F5D4),
                primaryContainer = Color(0xFF0D3E38),
                secondary = Color(0xFFFF007F),
                secondaryContainer = Color(0xFF4A0A28),
                tertiary = Color(0xFFB5179E),
                background = Color(0xFF0B0914),
                surface = Color(0xFF171329),
                surfaceElevated = Color(0xFF241C42),
                border = Color(0xFF3E275E),
                accentGlow = Color(0xFFFF007F),
                waveformColor = Color(0xFF00F5D4)
            )
            ARTISAN_BRONZE -> StudioColors(
                primary = Color(0xFF2E8B9A),
                primaryContainer = Color(0xFF164850),
                secondary = Color(0xFFD4A348),
                secondaryContainer = Color(0xFF4A3816),
                tertiary = Color(0xFFA5B4C2),
                background = Color(0xFF111317),
                surface = Color(0xFF181B22),
                surfaceElevated = Color(0xFF222631),
                border = Color(0xFF2F3545),
                accentGlow = Color(0xFFD4A348),
                waveformColor = Color(0xFF2E8B9A)
            )
            SOLAR_AMBER -> StudioColors(
                primary = Color(0xFFF59E0B),
                primaryContainer = Color(0xFF4A2E07),
                secondary = Color(0xFFEF4444),
                secondaryContainer = Color(0xFF451010),
                tertiary = Color(0xFFFBBF24),
                background = Color(0xFF12100E),
                surface = Color(0xFF1E1A17),
                surfaceElevated = Color(0xFF2C2520),
                border = Color(0xFF3D3228),
                accentGlow = Color(0xFFF59E0B),
                waveformColor = Color(0xFFF59E0B)
            )
        }
    }
}

val LocalStudioColors = staticCompositionLocalOf {
    StudioThemePalette.CYBER_FORGE.toStudioColors()
}

object StudioTheme {
    val colors: StudioColors
        @Composable
        get() = LocalStudioColors.current
}
