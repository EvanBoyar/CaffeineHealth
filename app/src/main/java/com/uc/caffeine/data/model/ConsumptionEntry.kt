package com.uc.caffeine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_CONSUMPTION_DURATION_MINUTES = 10
private const val MINUTE_IN_MILLIS = 60_000L

// This table stores EVERY drink the user logs — one row per drink consumed
// This is how we get:
//   - Today's total  (sum WHERE timestamp is today)
//   - History        (group by day)
//   - Half-life math (we know exact time each drink was consumed)
@Entity(tableName = "consumption_log")
data class ConsumptionEntry(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // We store the drink name + mg directly here (not a foreign key)
    // Why? Because if the user later deletes a preset, history should still show correctly
    val drinkName: String,
    val caffeineMg: Int,
    val emoji: String,

    // Stable preset identifier so we can recover units when editing later.
    val presetItemId: String = "",

    // The serving selection used for this logged entry.
    val quantity: Int = 1,
    val unitKey: String = "",
    val unitCaffeineMg: Double = 0.0,

    // for images of items
    val imageName: String = "",

    // Absorption rate in minutes - time to peak blood concentration
    // Stored here (not FK) so historical data remains accurate even if preset changes
    val absorptionRate: Int = 45,

    // Start time for this consumption window.
    // This is the canonical timestamp used for sorting, grouping, and display.
    val startedAtMillis: Long = System.currentTimeMillis(),

    // The number of whole minutes the user takes to finish the drink.
    val durationMinutes: Int = DEFAULT_CONSUMPTION_DURATION_MINUTES,
) {
    val normalizedDurationMinutes: Int
        get() = durationMinutes.coerceAtLeast(1)

    val finishedAtMillis: Long
        get() = startedAtMillis + (normalizedDurationMinutes * MINUTE_IN_MILLIS)
}
