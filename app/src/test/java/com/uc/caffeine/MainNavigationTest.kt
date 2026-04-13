package com.uc.caffeine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainNavigationTest {

    @Test
    fun resolveToolbarDestination_defaultsHomeForNullAndAddRoute() {
        assertEquals(AppDestinations.HOME, (null as AppRoute?).resolveToolbarDestination())
        assertEquals(AppDestinations.HOME, AddRoute.resolveToolbarDestination())
    }

    @Test
    fun resolveToolbarDestination_keepsTopLevelSelections() {
        assertEquals(AppDestinations.HOME, AppDestinations.HOME.resolveToolbarDestination())
        assertEquals(
            AppDestinations.ANALYTICS,
            AppDestinations.ANALYTICS.resolveToolbarDestination(),
        )
        assertEquals(
            AppDestinations.SETTINGS,
            AppDestinations.SETTINGS.resolveToolbarDestination(),
        )
    }

    @Test
    fun shouldShowHomeFab_onlyWhenHomeRouteIsActive() {
        assertTrue(AppDestinations.HOME.shouldShowHomeFab())
        assertFalse(AddRoute.shouldShowHomeFab())
        assertFalse(AppDestinations.ANALYTICS.shouldShowHomeFab())
        assertFalse(AppDestinations.SETTINGS.shouldShowHomeFab())
    }
}
