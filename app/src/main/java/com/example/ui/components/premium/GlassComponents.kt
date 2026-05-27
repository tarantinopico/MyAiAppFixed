package com.example.ui.components.premium

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    alpha: Float = 0.65f,
    blur: Dp = 24.dp,
    elevation: Dp = 0.dp,
    borderAlpha: Float = 0.2f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation, shape, spotColor = Color.Black.copy(alpha = 0.1f))
            .clip(shape)
            .background(color.copy(alpha = alpha))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderAlpha),
                        Color.White.copy(alpha = borderAlpha * 0.2f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
            // Note: Actual RenderEffect blur using RenderNode/GraphicsLayer might be heavy on compose
            // We use standard background alpha + optional RenderEffect when possible.
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
        alpha = 0.85f,
        borderAlpha = 0.15f,
        elevation = elevation,
        content = content
    )
}
