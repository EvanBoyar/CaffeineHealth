package com.uc.caffeine.util

import com.uc.caffeine.data.model.DrinkUnit
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.roundToInt

fun formatUnitLabel(unitKey: String): String {
    return unitKey.replace("(", " (")
}

fun formatServingSummary(
    quantity: Int,
    unitKey: String,
): String {
    val safeQuantity = quantity.coerceAtLeast(1)
    val label = pluralizeUnitKey(unitKey, safeQuantity)
    return "$safeQuantity ${formatUnitLabel(label)}"
}

fun calculateServingTotalCaffeine(
    quantity: Int,
    unitCaffeineMg: Double,
): Int {
    return (quantity.coerceAtLeast(1) * unitCaffeineMg).roundToInt()
}

fun formatCaffeineAmount(value: Double): String {
    val formatter = when {
        abs(value) < 1.0 -> DecimalFormat("0.##")
        abs(value - value.roundToInt()) < 0.001 -> DecimalFormat("0")
        else -> DecimalFormat("0.#")
    }
    return formatter.format(value)
}

fun findMatchingUnit(
    units: List<DrinkUnit>,
    unitKey: String,
    unitCaffeineMg: Double,
): DrinkUnit? {
    return units.firstOrNull { it.unitKey == unitKey }
        ?: units.firstOrNull { abs(it.caffeineMg - unitCaffeineMg) < 0.001 }
        ?: units.firstOrNull { it.isDefault }
        ?: units.firstOrNull()
}

private fun pluralizeUnitKey(
    unitKey: String,
    quantity: Int,
): String {
    if (quantity == 1) return unitKey

    val suffix = unitKey.substringAfter('(', missingDelimiterValue = "")
    val base = unitKey.substringBefore('(')

    val pluralBase = when (base) {
        "g" -> "g"
        "fl oz" -> "fl oz"
        "piece" -> "pieces"
        else -> "${base}s"
    }

    return if (suffix.isEmpty()) {
        pluralBase
    } else {
        "$pluralBase($suffix"
    }
}
