package com.uc.caffeine.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.name == value } ?: SYSTEM
        }
    }
}
