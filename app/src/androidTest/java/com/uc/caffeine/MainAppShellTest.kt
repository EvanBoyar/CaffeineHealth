package com.uc.caffeine

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.ui.theme.CaffeineTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainAppShellTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setContent() {
        composeRule.setContent {
            CaffeineTheme(dynamicColor = false) {
                Box(modifier = Modifier.requiredSize(width = 412.dp, height = 915.dp)) {
                    MainAppShell(userSettings = UserSettings(isOnboardingComplete = true))
                }
            }
        }
    }

    @Test
    fun fabOnlyShowsOnHome_andToolbarUsesAnalytics() {
        composeRule.onNodeWithTag("home_add_fab").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Analytics").assertIsDisplayed()
        composeRule.onAllNodesWithText("Add").assertCountEquals(0)

        composeRule.onNodeWithContentDescription("Analytics").performClick()
        composeRule.onNodeWithText("Analytics").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home_add_fab").assertCountEquals(0)

        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home_add_fab").assertCountEquals(0)

        composeRule.onNodeWithContentDescription("Home").performClick()
        composeRule.onNodeWithTag("home_add_fab").assertIsDisplayed()
    }

    @Test
    fun fabLaunchesAddScreen_andSuccessfulLogReturnsHome() {
        composeRule.onNodeWithTag("home_add_fab").performClick()
        composeRule.onNodeWithText("Add a drink").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home_add_fab").assertCountEquals(0)
        composeRule.onNodeWithText("Home").assertIsDisplayed()

        composeRule.onNodeWithText("Search drinks...", useUnmergedTree = true)
            .performTextInput("28 Black")

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(
                "28 Black Energy Drink",
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText(
            "28 Black Energy Drink",
            useUnmergedTree = true,
        )[0].performClick()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("Add entry").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Add entry").performClick()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(
                "Logged 28 Black Energy Drink",
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Logged 28 Black Energy Drink").assertIsDisplayed()
        composeRule.onNodeWithTag("home_add_fab").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
