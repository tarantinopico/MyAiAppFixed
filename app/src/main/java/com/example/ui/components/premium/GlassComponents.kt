package com.example.ui.components.premium

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppSettings

@Composable
fun BlurPanel(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppSettings.current
    val effectiveBlur = blurRadius * settings.blurIntensity

    // RenderEffect blur is only supported on Android 12+ (API 31), 
    // but compose `.blur()` automatically falls back to no-op if unsupported.
    Box(
        modifier = modifier.blur(radius = effectiveBlur)
    ) {
        content()
    }
}

@Composable
fun GlowSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large,
    elevation: Dp = 8.dp,
    glowRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppSettings.current
    val effectiveGlow = glowRadius * settings.glowIntensity

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = color.copy(alpha = settings.glowIntensity),
                ambientColor = color.copy(alpha = settings.glowIntensity * 0.5f)
            )
    ) {
        content()
    }
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    alpha: Float? = null,
    blur: Dp = 24.dp,
    elevation: Dp = 0.dp,
    borderAlpha: Float = 0.2f,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppSettings.current
    val effectiveAlpha = alpha ?: ((1f - settings.glassTransparency) * 0.9f).coerceIn(0.1f, 1f)

    Box(
        modifier = modifier
            .shadow(elevation, shape, spotColor = Color.Black.copy(alpha = 0.2f * settings.glowIntensity))
            .clip(shape)
            .background(color.copy(alpha = effectiveAlpha))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderAlpha * settings.glassTransparency),
                        Color.White.copy(alpha = borderAlpha * 0.2f * settings.glassTransparency),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

@Composable
fun DynamicGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        alpha = null, // Inherits global settings
        borderAlpha = 0.15f,
        elevation = elevation,
        content = content
    )
}

@Composable
fun AmbientGradientBackdrop(
    modifier: Modifier = Modifier
) {
    val settings = LocalAppSettings.current
    if (settings.enableAmbientGradient) {
        val primary = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f * settings.glowIntensity)
        val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f * settings.glowIntensity)
        val tertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f * settings.glowIntensity)

        Box(
            modifier = modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(tertiary, secondary, primary, Color.Transparent),
                        radius = 2000f
                    )
                )
        )
    } else {
        Box(modifier = modifier)
    }
}

