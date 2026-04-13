package com.uc.caffeine.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.uc.caffeine.ui.theme.CaffeineTheme
import com.uc.caffeine.util.AnalyticsRange
import com.uc.caffeine.util.AnalyticsUiState
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AnalyticsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var sourceProducer: CartesianChartModelProducer
    private lateinit var bedtimeProducer: CartesianChartModelProducer
    private lateinit var timeOfDayProducer: CartesianChartModelProducer

    @Before
    fun setUp() {
        sourceProducer = CartesianChartModelProducer()
        bedtimeProducer = CartesianChartModelProducer()
        timeOfDayProducer = CartesianChartModelProducer()

        runBlocking {
            sourceProducer.runTransaction {
                columnSeries {
                    series(180, 120, 90)
                }
            }
            bedtimeProducer.runTransaction {
                columnSeries {
                    series(12, 24, 36, 48, 42, 28, 16, 20, 32, 40, 30, 18)
                }
            }
            timeOfDayProducer.runTransaction {
                columnSeries {
                    series(40, 85, 60, 25)
                }
            }
        }
    }

    @Test
    fun analyticsScreenContent_rendersChartsAndUpdatesWhenRangeChanges() {
        composeRule.setContent {
            CaffeineTheme(dynamicColor = false) {
                val todayState = remember { testState(range = AnalyticsRange.TODAY, totalMg = 210, topSource = "Coffee") }
                val last30State = remember { testState(range = AnalyticsRange.LAST_30_DAYS, totalMg = 540, topSource = "Tea") }
                val last90State = remember { testState(range = AnalyticsRange.LAST_90_DAYS, totalMg = 999, topSource = "Energy Drinks") }
                var state by remember { mutableStateOf(todayState) }

                AnalyticsScreenContent(
                    uiState = state,
                    onRangeSelected = { range ->
                        state = when (range) {
                            AnalyticsRange.TODAY -> todayState
                            AnalyticsRange.YESTERDAY -> todayState
                            AnalyticsRange.LAST_30_DAYS -> last30State
                            AnalyticsRange.LAST_90_DAYS -> last90State
                            AnalyticsRange.CUSTOM -> last30State
                        }
                    },
                    sourceModelProducer = sourceProducer,
                    bedtimeModelProducer = bedtimeProducer,
                    timeOfDayModelProducer = timeOfDayProducer,
                )
            }
        }

        composeRule.onNodeWithText("Sources").assertIsDisplayed()
        composeRule.onNodeWithText("Bedtime impact").assertIsDisplayed()
        composeRule.onNodeWithText("When you drink caffeine").assertIsDisplayed()
        composeRule.onNodeWithText("210 mg").assertIsDisplayed()

        composeRule.onNodeWithText("90 Days").performClick()

        composeRule.onNodeWithText("999 mg").assertIsDisplayed()
        composeRule.onNodeWithText("Energy Drinks").assertIsDisplayed()
        composeRule.onAllNodesWithText("Analytics is on the way").assertCountEquals(0)
    }

    @Test
    fun analyticsScreenContent_showsEmptyStateWhenNoAnalyticsDataExists() {
        composeRule.setContent {
            CaffeineTheme(dynamicColor = false) {
                AnalyticsScreenContent(
                    uiState = AnalyticsUiState(
                        selectedRange = AnalyticsRange.LAST_30_DAYS,
                        hasData = false,
                    ),
                    onRangeSelected = {},
                    sourceModelProducer = sourceProducer,
                    bedtimeModelProducer = bedtimeProducer,
                    timeOfDayModelProducer = timeOfDayProducer,
                )
            }
        }

        composeRule.onNodeWithTag("analytics_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Nothing to chart yet").assertIsDisplayed()
        composeRule.onAllNodesWithText("Analytics is on the way").assertCountEquals(0)
    }

    private fun testState(
        range: AnalyticsRange,
        totalMg: Int,
        topSource: String,
    ): AnalyticsUiState {
        return AnalyticsUiState(
            selectedRange = range,
            hasData = true,
            totalCaffeineMg = totalMg,
            averageCaffeinePerDayMg = totalMg / 7,
            safeNights = if (range == AnalyticsRange.LAST_90_DAYS) 290 else 6,
            totalNights = when (range) {
                AnalyticsRange.TODAY -> 1
                AnalyticsRange.YESTERDAY -> 1
                AnalyticsRange.LAST_30_DAYS -> 30
                AnalyticsRange.LAST_90_DAYS -> 365
                AnalyticsRange.CUSTOM -> 30
            },
            topSourceLabel = topSource,
            sourceAxisLabels = listOf("Coffee", "Energy", "Tea"),
            sourceValues = listOf(180.0, 120.0, 90.0),
            bedtimeAxisLabels = when (range) {
                AnalyticsRange.TODAY -> listOf("Thu")
                AnalyticsRange.YESTERDAY -> listOf("Wed")
                AnalyticsRange.LAST_30_DAYS -> listOf("Mar 13", "Mar 20", "Mar 27", "Apr 3", "Apr 9")
                AnalyticsRange.LAST_90_DAYS -> listOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                )
                AnalyticsRange.CUSTOM -> listOf("Mar 13", "Mar 20", "Mar 27", "Apr 3", "Apr 9")
            },
            bedtimeValues = when (range) {
                AnalyticsRange.TODAY -> listOf(16.0)
                AnalyticsRange.YESTERDAY -> listOf(28.0)
                AnalyticsRange.LAST_30_DAYS -> listOf(18.0, 28.0, 34.0, 30.0, 22.0)
                AnalyticsRange.LAST_90_DAYS -> listOf(10.0, 12.0, 18.0, 22.0)
                AnalyticsRange.CUSTOM -> listOf(18.0, 28.0, 34.0, 30.0, 22.0)
            },
            timeOfDayAxisLabels = listOf("Night", "Morning", "Afternoon", "Evening"),
            timeOfDayValues = listOf(40.0, 85.0, 60.0, 25.0),
            sleepThresholdMg = 60.0,
        )
    }
}
