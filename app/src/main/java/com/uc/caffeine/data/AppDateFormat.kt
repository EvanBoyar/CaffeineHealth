package com.uc.caffeine.data

import java.util.Locale

enum class AppDateFormat(
    val pattern: String,
    val displayLabel: String,
) {
    MONTH_DAY_YEAR(
        pattern = "MM/dd/yyyy",
        displayLabel = "MM/DD/YYYY",
    ),
    DAY_MONTH_YEAR(
        pattern = "dd/MM/yyyy",
        displayLabel = "DD/MM/YYYY",
    ),
    YEAR_MONTH_DAY(
        pattern = "yyyy-MM-dd",
        displayLabel = "YYYY-MM-DD",
    );

    companion object {
        fun fromStorage(value: String?): AppDateFormat {
            return entries.firstOrNull { it.name == value } ?: fromLocale(Locale.getDefault())
        }

        fun fromLocale(locale: Locale): AppDateFormat {
            return when (locale.country.uppercase()) {
                "US", "CA", "PH" -> MONTH_DAY_YEAR
                "CN", "JP", "KR", "HU", "LT", "SE", "TW" -> YEAR_MONTH_DAY
                else -> DAY_MONTH_YEAR
            }
        }
    }
}
