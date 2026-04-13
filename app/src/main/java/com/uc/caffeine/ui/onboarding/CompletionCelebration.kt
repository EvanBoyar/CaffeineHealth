package com.uc.caffeine.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.uc.caffeine.ui.components.rememberAppHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CompletionCelebration(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberAppHaptics()

    val circleScale = remember { Animatable(0f) }
    val checkScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(20f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { haptics.celebration() }

        launch {
            circleScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            delay(200)
            checkScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        launch {
            delay(400)
            textAlpha.animateTo(1f, tween(300))
        }
        launch {
            delay(400)
            textOffset.animateTo(0f, tween(300, easing = EaseOut))
        }
        launch {
            delay(650)
            subtitleAlpha.animateTo(1f, tween(300))
        }
    }

    LaunchedEffect(Unit) {
        delay(2200)
        onComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(circleScale.value)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(checkScale.value),
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "You're all set!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffset.value
                },
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Welcome to Caffeine",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                },
            )
        }
    }
}
