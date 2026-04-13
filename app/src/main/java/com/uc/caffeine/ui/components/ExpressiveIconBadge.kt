package com.uc.caffeine.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun expressiveIconShape(index: Int): Shape =
    when (index.mod(4)) {
        0 -> MaterialShapes.Cookie6Sided.toShape()
        1 -> RoundedCornerShape(16.dp)
        2 -> CircleShape
        else -> RoundedCornerShape(999.dp)
    }

@Composable
fun ExpressiveIconBadge(
    index: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier.size(size),
        shape = expressiveIconShape(index),
        color = containerColor ?: MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
