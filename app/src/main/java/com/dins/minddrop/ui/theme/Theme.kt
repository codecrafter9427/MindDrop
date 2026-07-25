package com.dins.minddrop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dins.minddrop.domain.model.Priority

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = Mauve40,
    onTertiary = Color.White,
    tertiaryContainer = Mauve90,
    onTertiaryContainer = Mauve10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = NeutralLight,
    onBackground = OnNeutralLight,
    surface = NeutralLight,
    onSurface = OnNeutralLight,
    surfaceVariant = NeutralSurfaceLight,
    onSurfaceVariant = Slate40,
    outline = Slate40
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo20,
    primaryContainer = Indigo30,
    onPrimaryContainer = Indigo90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate20,
    onSecondaryContainer = Slate90,
    tertiary = Mauve80,
    onTertiary = Mauve20,
    tertiaryContainer = Mauve20,
    onTertiaryContainer = Mauve90,
    error = Red80,
    onError = Red10,
    errorContainer = Red40,
    onErrorContainer = Red90,
    background = NeutralDark,
    onBackground = OnNeutralDark,
    surface = NeutralDark,
    onSurface = OnNeutralDark,
    surfaceVariant = NeutralSurfaceDark,
    onSurfaceVariant = Slate80,
    outline = Slate80
)

@Composable
fun MindDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: dynamic color would replace the brand palette above with
    // colours derived from the user's wallpaper, which defeats having a custom
    // scheme at all. Left as a parameter so it can be opted into per-screen.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Priority colours live outside [MaterialTheme.colorScheme] because they carry
 * their own fixed semantics (red means urgent regardless of the brand palette),
 * but they still need to swap per theme to stay legible on both surfaces.
 */
@Composable
@ReadOnlyComposable
fun priorityColor(priority: Priority): Color {
    val dark = isSystemInDarkTheme()
    return when (priority) {
        Priority.URGENT -> if (dark) PriorityUrgentDark else PriorityUrgentLight
        Priority.HIGH -> if (dark) PriorityHighDark else PriorityHighLight
        Priority.NORMAL -> if (dark) PriorityNormalDark else PriorityNormalLight
        Priority.LOW -> if (dark) PriorityLowDark else PriorityLowLight
    }
}
