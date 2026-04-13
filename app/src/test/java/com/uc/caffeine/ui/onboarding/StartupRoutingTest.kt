package com.uc.caffeine.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupRoutingTest {

    @Test
    fun resolveStartupDestination_returnsLoadingWhileRequiredStateIsStillLoading() {
        assertEquals(
            StartupDestination.Loading,
            resolveStartupDestination(
                isSettingsLoaded = false,
                isConsumptionHistoryLoading = true,
                hasExistingConsumptionHistory = false,
                isOnboardingComplete = false,
            ),
        )
    }

    @Test
    fun resolveStartupDestination_routesFreshInstallToOnboarding() {
        assertEquals(
            StartupDestination.Onboarding,
            resolveStartupDestination(
                isSettingsLoaded = true,
                isConsumptionHistoryLoading = false,
                hasExistingConsumptionHistory = false,
                isOnboardingComplete = false,
            ),
        )
    }

    @Test
    fun resolveStartupDestination_routesCompletedUsersToMain() {
        assertEquals(
            StartupDestination.Main,
            resolveStartupDestination(
                isSettingsLoaded = true,
                isConsumptionHistoryLoading = false,
                hasExistingConsumptionHistory = false,
                isOnboardingComplete = true,
            ),
        )
    }

    @Test
    fun resolveStartupDestination_routesLegacyUsersWithHistoryToMain() {
        assertEquals(
            StartupDestination.Main,
            resolveStartupDestination(
                isSettingsLoaded = true,
                isConsumptionHistoryLoading = false,
                hasExistingConsumptionHistory = true,
                isOnboardingComplete = false,
            ),
        )
    }
}
