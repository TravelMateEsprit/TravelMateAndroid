package com.travelmate.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimaryDark,
    secondary = ColorSecondaryDark,
    tertiary = ColorAccent,
    background = ColorBackgroundDark,
    surface = ColorSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = ColorTextPrimaryDark,
    onBackground = ColorTextPrimaryDark,
    onSurface = ColorTextPrimaryDark,
    onSurfaceVariant = ColorTextSecondaryDark,
    error = ColorError,
    outline = ColorDividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    secondary = ColorSecondary,
    tertiary = ColorAccent,
    background = ColorBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = ColorTextPrimaryLight,
    onBackground = ColorTextPrimaryLight,
    onSurface = ColorTextPrimaryLight,
    onSurfaceVariant = ColorTextSecondaryLight,
    error = ColorError,
    outline = ColorDivider
)

@Composable
fun TravelMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Configure system bars properly
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light status bar icons for light theme, dark icons for dark theme
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
            
            // Ensure system bars are always visible
            insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
