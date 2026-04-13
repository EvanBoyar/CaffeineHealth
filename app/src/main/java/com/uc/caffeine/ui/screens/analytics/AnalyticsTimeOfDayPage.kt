package com.uc.caffeine.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.uc.caffeine.ui.components.SettingsPageScaffold
import com.uc.caffeine.ui.components.rememberAppHaptics
import com.uc.caffeine.util.AnalyticsRange
import com.uc.caffeine.util.AnalyticsUiState
import java.time.LocalDate
import androidx.compose.material3.Text

private val timeOfDayBucketDetails = listOf(
    "Night" to "7pm – 5am",
    "Morning" to "5am – 12pm",
    "Afternoon" to "12pm – 3pm",
    "Evening" to "3pm – 7pm",
)

@Composable
internal fun AnalyticsTimeOfDayPage(
    uiState: AnalyticsUiState,
    onRangeSelected: (AnalyticsRange) -> Unit,
    onCustomRange: (LocalDate, LocalDate) -> Unit,
    onBack: () -> Unit,
    modelProducer: CartesianChartModelProducer,
) {
    val haptics = rememberAppHaptics()
    var showRangeSheet by remember { mutableStateOf(false) }

    SettingsPageScaffold(title = "When you drink caffeine", showBackButton = true, onBack = onBack) { bottomPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AnalyticsRangeButton(
                    selectedRange = uiState.selectedRange,
                    customStartDate = uiState.customStartDate,
                    customEndDate = uiState.customEndDate,
                    onClick = { showRangeSheet = true },
                )
            }

            if (uiState.hasData) {
                item {
                    AnalyticsChartCard(
                        title = "When you drink caffeine",
                        supportingText = "How your intake is spread across the day.",
                    ) {
                        AnalyticsColumnChart(
                            axisLabels = uiState.timeOfDayAxisLabels,
                            modelProducer = modelProducer,
                            columnProvider = rememberSingleColumnProvider(
                                color = MaterialTheme.colorScheme.secondary,
                                width = 18.dp,
                            ),
                        )
                    }
                }

                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AnalyticsCardShape,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            timeOfDayBucketDetails.forEach { (name, range) ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = range,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    AnalyticsEmptyState(selectedRange = uiState.selectedRange)
                }
            }
        }
    }

    if (showRangeSheet) {
        AnalyticsRangeBottomSheet(
            selectedRange = uiState.selectedRange,
            onRangeSelected = { range ->
                haptics.toggle()
                onRangeSelected(range)
            },
            onCustomRange = { start, end ->
                haptics.toggle()
                onCustomRange(start, end)
            },
            onDismiss = { showRangeSheet = false },
        )
    }
}
