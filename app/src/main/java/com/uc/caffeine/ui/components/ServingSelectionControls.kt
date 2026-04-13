package com.uc.caffeine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uc.caffeine.data.model.DrinkUnit
import com.uc.caffeine.util.formatCaffeineAmount
import com.uc.caffeine.util.formatUnitLabel

@Composable
fun ServingQuantityStepper(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RollingNumberText(
            text = quantity.toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
            ),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onDecrement,
                enabled = quantity > 1,
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease quantity",
                    modifier = Modifier.size(26.dp),
                )
            }
            IconButton(onClick = onIncrement) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase quantity",
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
fun ServingUnitSelector(
    units: List<DrinkUnit>,
    selectedUnit: DrinkUnit?,
    onUnitSelected: (DrinkUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        units.forEachIndexed { index, unit ->
            val isSelected = selectedUnit?.unitKey == unit.unitKey
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.RadioButton,
                        onClick = { onUnitSelected(unit) },
                    ),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatUnitLabel(unit.unitKey),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${formatCaffeineAmount(unit.caffeineMg)} mg",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                        )
                    }
                }
            }

            if (index < units.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}
