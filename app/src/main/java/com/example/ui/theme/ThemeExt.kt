package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.data.repository.AppSettings

val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueDark,
    secondary = AccentPurpleDark,
    tertiary = AccentTealDark,
    background = BgDark,
    surface = BgDarkSurface,
    surfaceVariant = BgDarkSurfaceVariant,
    onPrimary = TextPrimaryDark,
    onSecondary = TextPrimaryDark,
    onTertiary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineColorDark,
    outlineVariant = OutlineColorDark.copy(alpha = 0.05f)
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentPurple,
    tertiary = AccentTeal,
    background = BgLight,
    surface = BgLightSurface,
    surfaceVariant = BgLightSurfaceVariant,
    onPrimary = TextPrimaryDark,
    onSecondary = TextPrimaryDark,
    onTertiary = TextPrimaryDark,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineColorLight,
    outlineVariant = OutlineColorLight.copy(alpha = 0.05f)
)

@Composable
fun AIModelAggregatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    appSettings: AppSettings = AppSettings(),
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val wic = WindowCompat.getInsetsController(window, view)
            wic.isAppearanceLightStatusBars = !darkTheme
            wic.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val radiusScale = appSettings.scaleCornerRadius
    val dynamicShapes = Shapes(
        small = RoundedCornerShape((12 * radiusScale).dp),
        medium = RoundedCornerShape((20 * radiusScale).dp),
        large = RoundedCornerShape((26 * radiusScale).dp),
        extraLarge = RoundedCornerShape((32 * radiusScale).dp)
    )

    CompositionLocalProvider(LocalAppSettings provides appSettings) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = dynamicShapes,
            content = content
        )
    }
}

