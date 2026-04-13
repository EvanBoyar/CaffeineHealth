package com.uc.caffeine.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.ui.components.SettingsPageScaffold
import com.uc.caffeine.ui.components.rememberAppHaptics
import com.uc.caffeine.ui.onboarding.SleepTimePickerCard
import com.uc.caffeine.util.formatTimeOfDay
import java.time.LocalTime

@Composable
internal fun CaffeineProfileSettingsScreen(
    userSettings: UserSettings,
    onBack: () -> Unit,
    onHalfLifeChange: (Int) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onThresholdChange: (Int) -> Unit,
    onRedoOnboarding: () -> Unit,
) {
    val halfLifeHours = userSettings.halfLifeMinutes / 60
    val bedtime = formatTimeOfDay(
        hour = userSettings.sleepTimeHour,
        minute = userSettings.sleepTimeMinute,
        settings = userSettings,
    )

    SettingsPageScaffold(
        title = "Caffeine Profile",
        showBackButton = true,
        onBack = onBack,
    ) { bottomPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding + 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "These settings shape how Caffeine estimates your metabolism and bedtime impact throughout the day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Caffeine Profile",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "Fine-tune the inputs behind your active caffeine curve and sleep guidance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.86f),
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProfileSnapshotMetric(
                        title = "Estimated half-life",
                        value = "$halfLifeHours hr",
                        description = "How long caffeine tends to linger in your system.",
                    )
                    HorizontalDivider()
                    ProfileSnapshotMetric(
                        title = "Typical bedtime",
                        value = bedtime,
                        description = "Used for the bedtime marker and sleep prediction.",
                    )
                    HorizontalDivider()
                    ProfileSnapshotMetric(
                        title = "Sleep threshold",
                        value = "${userSettings.sleepThresholdMg} mg",
                        description = "The active-caffeine target the sleep guidance compares against.",
                    )
                }
            }

            ExpressiveStepperCard(
                title = "Caffeine Half-Life",
                value = "$halfLifeHours hr",
                supportingText = "Average is around 5 hours, but you can make it more personal here.",
                hint = "Adjust in 1-hour steps",
                onDecrease = {
                    onHalfLifeChange((halfLifeHours - 1).coerceIn(2, 12))
                },
                onIncrease = {
                    onHalfLifeChange((halfLifeHours + 1).coerceIn(2, 12))
                },
                decreaseEnabled = halfLifeHours > 2,
                increaseEnabled = halfLifeHours < 12,
            )

            SleepTimePickerCard(
                displaySettings = userSettings,
                selectedTime = LocalTime.of(
                    userSettings.sleepTimeHour,
                    userSettings.sleepTimeMinute,
                ),
                onSleepTimeChanged = { time ->
                    onTimeChange(time.hour, time.minute)
                },
            )

            ExpressiveStepperCard(
                title = "Sleep Threshold",
                value = "${userSettings.sleepThresholdMg} mg",
                supportingText = "Keep this lower if caffeine affects your sleep easily, higher if your usual cutoff is more forgiving.",
                hint = "Adjust in 5 mg steps",
                onDecrease = {
                    onThresholdChange((userSettings.sleepThresholdMg - 5).coerceIn(20, 200))
                },
                onIncrease = {
                    onThresholdChange((userSettings.sleepThresholdMg + 5).coerceIn(20, 200))
                },
                decreaseEnabled = userSettings.sleepThresholdMg > 20,
                increaseEnabled = userSettings.sleepThresholdMg < 200,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Redo onboarding",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Walk through the profile setup again without deleting any logged drinks or history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = onRedoOnboarding,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                        )
                        Text(
                            text = "Redo onboarding",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSnapshotMetric(
    title: String,
    value: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpressiveStepperCard(
    title: String,
    value: String,
    supportingText: String,
    hint: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
) {
    val haptics = rememberAppHaptics()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = {
                        haptics.toggle()
                        onDecrease()
                    },
                    enabled = decreaseEnabled,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease $title",
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        haptics.toggle()
                        onIncrease()
                    },
                    enabled = increaseEnabled,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase $title",
                    )
                }
            }
        }
    }
}
