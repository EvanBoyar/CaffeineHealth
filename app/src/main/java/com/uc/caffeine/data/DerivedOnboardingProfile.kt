package com.uc.caffeine.data

data class DerivedOnboardingProfile(
    val halfLifeMinutes: Int,
    val sleepThresholdMg: Int,
    val sleepTimeHour: Int,
    val sleepTimeMinute: Int,
)

/**
 * Raw onboarding answers stored as primitives for DataStore persistence.
 * Kept separate from the onboarding UI enums to avoid data→ui package dependency.
 */
data class ProfileFactors(
    val ageBucket: String? = null,
    val weightValue: Int = 60,
    val weightUnit: String = "Kilograms",
    val hasInsomnia: Boolean? = null,
    val smokingHabit: String? = null,
    val heavyAlcohol: Boolean? = null,
    val heavyCaffeine: Boolean? = null,
    val liverDisease: String? = null,
    val medications: Set<String> = emptySet(),
)
