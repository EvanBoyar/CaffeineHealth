package com.uc.caffeine.data

data class DerivedOnboardingProfile(
    val halfLifeMinutes: Int,
    val sleepThresholdMg: Int,
    val sleepTimeHour: Int,
    val sleepTimeMinute: Int,
)
