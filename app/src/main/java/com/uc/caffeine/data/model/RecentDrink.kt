package com.uc.caffeine.data.model

// This is NOT a database table (@Entity) — it has no @Entity annotation
// It's a query result class — Room maps the columns from our SQL query into this shape
// The field names MUST match the column names/aliases in the SQL exactly
data class RecentDrink(
    val drinkName: String,
    val caffeineMg: Int,
    val emoji: String,
    val presetItemId: String,
    val quantity: Int,
    val unitKey: String,
    val unitCaffeineMg: Double,
    val imageName: String,
    val absorptionRate: Int,
    val durationMinutes: Int,
    val lastUsed: Long  // MAX(timestamp) aliased as lastUsed in the query
)
