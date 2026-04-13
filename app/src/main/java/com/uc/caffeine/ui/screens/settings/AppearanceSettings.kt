package com.uc.caffeine.ui.screens.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uc.caffeine.data.ThemeMode
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.ui.components.SettingsPageScaffold
import com.uc.caffeine.ui.components.rememberAppHaptics

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
internal fun AppearanceSettingsScreen(
    userSettings: UserSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val themeModes = listOf(
        ThemeMode.SYSTEM to "System",
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark",
    )

    SettingsPageScaffold(
        title = "Appearance",
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
                text = "Control how Caffeine looks across the app, from overall theme mode to Material You colors.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Choose whether the app follows your device theme or stays in a fixed light or dark mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    themeModes.forEachIndexed { index, (themeMode, label) ->
                        ToggleButton(
                            checked = userSettings.themeMode == themeMode,
                            onCheckedChange = { checked ->
                                if (checked && userSettings.themeMode != themeMode) {
                                    haptics.toggle()
                                    onThemeModeChange(themeMode)
                                }
                            },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                themeModes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }

            SegmentedListItem(
                onClick = {
                    haptics.toggle()
                    onDynamicColorChange(!userSettings.useDynamicColor)
                },
                content = {
                    Text(text = "Dynamic Color")
                },
                supportingContent = {
                    Text(text = "Use your wallpaper colors for the app theme")
                },
                trailingContent = {
                    Switch(
                        checked = userSettings.useDynamicColor,
                        onCheckedChange = { enabled ->
                            haptics.toggle()
                            onDynamicColorChange(enabled)
                        },
                    )
                },
                shapes = ListItemDefaults.segmentedShapes(
                    index = 0,
                    count = 1,
                ),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        }
    }
}
