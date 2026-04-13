package com.uc.caffeine.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uc.caffeine.ui.theme.CaffeineSurfaceDefaults

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SegmentedListGroup(
    items: List<T>,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    itemModifier: Modifier = Modifier,
    itemSpacing: Dp = 6.dp,
    leadingContent: (@Composable (T) -> Unit)? = null,
    content: @Composable (T) -> Unit,
    supportingContent: (@Composable (T) -> Unit)? = null,
    trailingContent: (@Composable (T) -> Unit)? = null,
) {
    val resolvedContainerColor = containerColor ?: CaffeineSurfaceDefaults.groupedListContainerColor

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        items.forEachIndexed { index, item ->
            SegmentedListItem(
                modifier = itemModifier,
                onClick = { onItemClick(item) },
                leadingContent = leadingContent?.let { { it(item) } },
                content = { content(item) },
                supportingContent = supportingContent?.let { { it(item) } },
                trailingContent = trailingContent?.let { { it(item) } },
                shapes = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = items.size,
                ),
                colors = ListItemDefaults.colors(
                    containerColor = resolvedContainerColor,
                ),
            )
        }
    }
}
