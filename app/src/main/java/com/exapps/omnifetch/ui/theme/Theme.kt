package com.exapps.omnifetch.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = DarkBackground,
    primaryContainer = Blue700,
    onPrimaryContainer = Blue200,
    secondary = Teal500,
    onSecondary = DarkBackground,
    secondaryContainer = Teal500,
    onSecondaryContainer = Teal200,
    tertiary = Amber500,
    tertiaryContainer = Amber200,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DownloadError,
    onError = DarkBackground,
    outline = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = LightSurface,
    primaryContainer = Blue200,
    onPrimaryContainer = Blue700,
    secondary = Teal500,
    onSecondary = LightSurface,
    secondaryContainer = Teal200,
    onSecondaryContainer = Teal500,
    tertiary = Amber500,
    tertiaryContainer = Amber200,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DarkBackground,
    error = DownloadError,
    onError = LightSurface,
    outline = DarkOnSurfaceVariant
)

@Composable
fun OmniFetchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
