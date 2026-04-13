package com.uc.caffeine.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uc.caffeine.ui.components.CaffeineScreenScaffold
import com.uc.caffeine.ui.components.rememberAppHaptics
import com.uc.caffeine.util.AnalyticsRange
import com.uc.caffeine.util.AnalyticsUiState
import kotlin.math.roundToInt

private data class AnalyticsNavItem(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AnalyticsMainPage(
    uiState: AnalyticsUiState,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onSourcesClick: () -> Unit,
    onBedtimeClick: () -> Unit,
    onTimeOfDayClick: () -> Unit,
) {
    val haptics = rememberAppHaptics()

    CaffeineScreenScaffold(
        title = "Analytics",
    ) { bottomPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AnalyticsRangePicker(
                    selectedRange = uiState.selectedRange,
                    onRangeSelected = { range ->
                        haptics.toggle()
                        onRangeSelected(range)
                    },
                )
            }

            if (uiState.hasData) {
                item {
                    AnalyticsSummaryCard(uiState = uiState)
                }
                item {
                    AnalyticsNavCard(
                        onSourcesClick = {
                            haptics.navigation()
                            onSourcesClick()
                        },
                        onBedtimeClick = {
                            haptics.navigation()
                            onBedtimeClick()
                        },
                        onTimeOfDayClick = {
                            haptics.navigation()
                            onTimeOfDayClick()
                        },
                    )
                }
            } else {
                item {
                    AnalyticsEmptyState(selectedRange = uiState.selectedRange)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnalyticsNavCard(
    onSourcesClick: () -> Unit,
    onBedtimeClick: () -> Unit,
    onTimeOfDayClick: () -> Unit,
) {
    val items = listOf(
        AnalyticsNavItem(
            title = "Caffeine by Source",
            summary = "Where your caffeine came from over this period.",
            icon = Icons.Filled.PieChart,
            onClick = onSourcesClick,
        ),
        AnalyticsNavItem(
            title = "Bedtime Impact",
            summary = "How much caffeine is still active by the time you sleep.",
            icon = Icons.Filled.Bedtime,
            onClick = onBedtimeClick,
        ),
        AnalyticsNavItem(
            title = "When You Drink Caffeine",
            summary = "How your intake is spread across the day.",
            icon = Icons.Filled.Schedule,
            onClick = onTimeOfDayClick,
        ),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.forEachIndexed { index, item ->
            SegmentedListItem(
                onClick = item.onClick,
                leadingContent = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                content = {
                    Text(text = item.title)
                },
                supportingContent = {
                    Text(text = item.summary)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                    )
                },
                shapes = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = items.size,
                ),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        }
    }
}

@Composable
private fun AnalyticsSummaryCard(uiState: AnalyticsUiState) {
    val contentColor = MaterialTheme.colorScheme.onTertiaryContainer

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AnalyticsCardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            DecorativeShapesBackground(
                color = contentColor,
                modifier = Modifier
                    .matchParentSize()
                    .clip(AnalyticsCardShape),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = analyticsHeadlineForRange(uiState.selectedRange),
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.8f),
                    )
                    Text(
                        text = "${uiState.totalCaffeineMg} mg",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                    Text(
                        text = "Total caffeine logged in this range",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.82f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryMetric(
                        label = "Average/day",
                        value = "${uiState.averageCaffeinePerDayMg} mg",
                        modifier = Modifier.weight(1f),
                        contentColor = contentColor,
                    )
                    SummaryMetric(
                        label = "Safe nights",
                        value = "${uiState.safeNights} / ${uiState.totalNights}",
                        modifier = Modifier.weight(1f),
                        contentColor = contentColor,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryMetric(
                        label = "Top source",
                        value = uiState.topSourceLabel,
                        modifier = Modifier.weight(1f),
                        contentColor = contentColor,
                    )
                    SummaryMetric(
                        label = "Sleep threshold",
                        value = "${uiState.sleepThresholdMg.roundToInt()} mg",
                        modifier = Modifier.weight(1f),
                        contentColor = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    contentColor: Color,
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.72f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DecorativeShapesBackground(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val shapeAlpha = 0.07f
    val transition = rememberInfiniteTransition(label = "decorShapes")

    val drift1x by transition.animateFloat(
        initialValue = 0f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(7200, easing = LinearEasing), RepeatMode.Reverse),
        label = "d1x",
    )
    val drift1y by transition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(8500, easing = LinearEasing), RepeatMode.Reverse),
        label = "d1y",
    )
    val drift2x by transition.animateFloat(
        initialValue = 0f, targetValue = -14f,
        animationSpec = infiniteRepeatable(tween(9100, easing = LinearEasing), RepeatMode.Reverse),
        label = "d2x",
    )
    val drift2y by transition.animateFloat(
        initialValue = 0f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(6800, easing = LinearEasing), RepeatMode.Reverse),
        label = "d2y",
    )
    val drift3x by transition.animateFloat(
        initialValue = 0f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "d3x",
    )
    val drift3y by transition.animateFloat(
        initialValue = 0f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(7600, easing = LinearEasing), RepeatMode.Reverse),
        label = "d3y",
    )
    val drift4x by transition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(9400, easing = LinearEasing), RepeatMode.Reverse),
        label = "d4x",
    )
    val drift4y by transition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "d4y",
    )

    Box(modifier = modifier.clearAndSetSemantics {}) {
        // Top-right Cookie12Sided
        Box(
            modifier = Modifier
                .size(110.dp)
                .offset(x = (260 + drift1x).dp, y = (-30 + drift1y).dp)
                .clip(MaterialShapes.Cookie12Sided.toShape())
                .background(color.copy(alpha = shapeAlpha)),
        )
        // Bottom-left Cookie6Sided
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = (-20 + drift2x).dp, y = (120 + drift2y).dp)
                .clip(MaterialShapes.Cookie6Sided.toShape())
                .background(color.copy(alpha = shapeAlpha)),
        )
        // Top-left Arch
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(x = (-10 + drift3x).dp, y = (-15 + drift3y).dp)
                .clip(MaterialShapes.Arch.toShape())
                .background(color.copy(alpha = shapeAlpha)),
        )
        // Bottom-right Cookie6Sided
        Box(
            modifier = Modifier
                .size(75.dp)
                .offset(x = (300 + drift4x).dp, y = (130 + drift4y).dp)
                .clip(MaterialShapes.Cookie6Sided.toShape())
                .background(color.copy(alpha = shapeAlpha)),
        )
    }
}

private fun analyticsHeadlineForRange(range: AnalyticsRange): String = when (range) {
    AnalyticsRange.TODAY -> "Today"
    AnalyticsRange.YESTERDAY -> "Yesterday"
    AnalyticsRange.LAST_30_DAYS -> "Last 30 days"
    AnalyticsRange.LAST_90_DAYS -> "Last 90 days"
    AnalyticsRange.CUSTOM -> "Custom range"
}
