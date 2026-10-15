package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.settings.AccentTheme
import com.example.data.settings.ThemeMode

fun buildCustomColorScheme(
    isDark: Boolean,
    isAmoled: Boolean,
    accent: AccentTheme
): ColorScheme {
    val (primary, secondary, container) = when (accent) {
        AccentTheme.CYAN -> Triple(AccentCyanPrimary, AccentCyanSecondary, AccentCyanContainer)
        AccentTheme.EMERALD -> Triple(AccentEmeraldPrimary, AccentEmeraldSecondary, AccentEmeraldContainer)
        AccentTheme.INDIGO -> Triple(AccentIndigoPrimary, AccentIndigoSecondary, AccentIndigoContainer)
        AccentTheme.PURPLE -> Triple(AccentPurplePrimary, AccentPurpleSecondary, AccentPurpleContainer)
        AccentTheme.AMBER -> Triple(AccentAmberPrimary, AccentAmberSecondary, AccentAmberContainer)
        AccentTheme.ROSE -> Triple(AccentRosePrimary, AccentRoseSecondary, AccentRoseContainer)
        AccentTheme.SLATE -> Triple(AccentSlatePrimary, AccentSlateSecondary, AccentSlateContainer)
        else -> Triple(AccentCyanPrimary, AccentCyanSecondary, AccentCyanContainer) // Default to Muzli Teal-Cyan
    }

    return if (isDark) {
        val bg = if (isAmoled) PureBlack else DarkSurfaceDim
        val surf = if (isAmoled) Color(0xFF090C12) else DarkSurface
        val containerSurf = if (isAmoled) Color(0xFF10151F) else DarkSurfaceContainer
        val highContainerSurf = if (isAmoled) Color(0xFF18202E) else DarkSurfaceContainerHigh

        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF041311),
            primaryContainer = container,
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = containerSurf,
            onSecondaryContainer = Color(0xFFF3F4F6),
            tertiary = StatusWarning,
            onTertiary = Color.Black,
            background = bg,
            onBackground = Color(0xFFF8FAFC),
            surface = surf,
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = containerSurf,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B),
            surfaceContainer = containerSurf,
            surfaceContainerHigh = highContainerSurf,
            surfaceContainerHighest = if (isAmoled) Color(0xFF222D40) else DarkSurfaceContainerHighest,
            error = StatusCritical,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0F7F4),
            onPrimaryContainer = Color(0xFF064E3B),
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = LightSurfaceContainer,
            onSecondaryContainer = Color(0xFF1F2937),
            tertiary = StatusSafe,
            onTertiary = Color.White,
            background = LightBackground,
            onBackground = Color(0xFF0F172A),
            surface = LightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightSurfaceContainerHigh,
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0),
            surfaceContainer = LightSurfaceContainer,
            surfaceContainerHigh = LightSurfaceContainerHigh,
            surfaceContainerHighest = LightSurfaceContainerHighest,
            error = StatusCritical,
            onError = Color.White
        )
    }
}

@Composable
fun AttendEaseTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    accentTheme: AccentTheme = AccentTheme.CYAN,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val isAmoled = themeMode == ThemeMode.AMOLED
    val context = LocalContext.current

    val canUseDynamic = dynamicColor &&
            accentTheme == AccentTheme.DYNAMIC &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        canUseDynamic -> {
            val base = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isAmoled) {
                base.copy(
                    background = PureBlack,
                    surface = Color(0xFF090C12),
                    surfaceContainer = Color(0xFF10151F),
                    surfaceContainerHigh = Color(0xFF18202E),
                    surfaceContainerHighest = Color(0xFF222D40)
                )
            } else {
                base
            }
        }
        else -> buildCustomColorScheme(isDark = isDark, isAmoled = isAmoled, accent = accentTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = AttendEaseTheme(
    themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
    dynamicColor = dynamicColor,
    content = content
)
