package com.uc.caffeine.ui.onboarding

enum class StartupDestination {
    Loading,
    Onboarding,
    Main,
}

fun resolveStartupDestination(
    isSettingsLoaded: Boolean,
    isConsumptionHistoryLoading: Boolean,
    hasExistingConsumptionHistory: Boolean,
    isOnboardingComplete: Boolean,
): StartupDestination {
    return when {
        !isSettingsLoaded || isConsumptionHistoryLoading -> StartupDestination.Loading
        isOnboardingComplete || hasExistingConsumptionHistory -> StartupDestination.Main
        else -> StartupDestination.Onboarding
    }
}
