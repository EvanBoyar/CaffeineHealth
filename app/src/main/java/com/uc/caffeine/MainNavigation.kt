package com.uc.caffeine

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import java.io.Serializable

sealed interface AppRoute : NavKey

enum class AppDestinations(
    val label: String,
    val iconOutlinedRes: Int? = null,
    val iconFilledRes: Int? = null,
    val iconOutlinedVector: ImageVector? = null,
    val iconFilledVector: ImageVector? = null,
) : AppRoute {
    HOME(
        label = "Home",
        iconOutlinedRes = R.drawable.ic_home,
        iconFilledRes = R.drawable.ic_home_filled,
    ),
    ANALYTICS(
        label = "Analytics",
        iconOutlinedVector = Icons.Outlined.Analytics,
        iconFilledVector = Icons.Filled.Analytics,
    ),
    SETTINGS(
        label = "Settings",
        iconOutlinedVector = Icons.Outlined.Settings,
        iconFilledVector = Icons.Filled.Settings,
    ),
}

object AddRoute : AppRoute, Serializable

internal val toolbarDestinations = listOf(
    AppDestinations.HOME,
    AppDestinations.ANALYTICS,
    AppDestinations.SETTINGS,
)

internal fun AppRoute?.resolveToolbarDestination(): AppDestinations {
    return when (this) {
        null,
        AddRoute,
        AppDestinations.HOME -> AppDestinations.HOME

        AppDestinations.ANALYTICS -> AppDestinations.ANALYTICS
        AppDestinations.SETTINGS -> AppDestinations.SETTINGS
    }
}

internal fun AppRoute?.shouldShowHomeFab(): Boolean = this == AppDestinations.HOME
