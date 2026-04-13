package com.uc.caffeine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uc.caffeine.LocalAppScaffoldPadding // Import your new global

@Composable
fun CaffeineScreenScaffold(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    headerBottomSpacing: Dp = 16.dp,
    // Provide bottomPadding to the content block so lists know how much to offset
    content: @Composable ColumnScope.(bottomPadding: Dp) -> Unit
) {
    val appPadding = LocalAppScaffoldPadding.current

    Column(
        modifier = modifier
            .fillMaxSize()
            // 1. ADD THIS LINE: Explicitly push the content below the status bar / notch!
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(
                start = contentPadding.calculateLeftPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateRightPadding(LocalLayoutDirection.current),
                top = contentPadding.calculateTopPadding()
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (headerBottomSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(headerBottomSpacing))
        }

        // 3. Pass the combined bottom padding (Nav Bar + Pill height) down!
        // Because we don't pad the bottom of the Column, backgrounds draw edge-to-edge.
        content(appPadding.calculateBottomPadding())
    }
}