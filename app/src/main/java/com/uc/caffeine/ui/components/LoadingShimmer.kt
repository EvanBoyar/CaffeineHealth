package com.uc.caffeine.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

fun Modifier.shimmerEffect(
    shape: Shape,
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "loading-shimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loading-shimmer-progress",
    )

    val width = size.width.coerceAtLeast(1).toFloat()
    val height = size.height.coerceAtLeast(1).toFloat()
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.82f)
    val highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val brush = remember(width, height, shimmerProgress, baseColor, highlightColor) {
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(x = width * shimmerProgress - width, y = 0f),
            end = Offset(x = width * shimmerProgress, y = height),
        )
    }

    background(
        brush = brush,
        shape = shape,
    ).onSizeChanged { size = it }
}
