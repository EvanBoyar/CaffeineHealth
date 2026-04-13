package com.uc.caffeine.ui.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.ui.theme.CaffeineTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun smokeFlow_reachesProfileReadyWithoutCrashing() {
        setSizedContent(width = 412.dp, height = 915.dp) {
            TestOnboardingFlowHost()
        }

        composeRule.onNodeWithText("Start profiling").performClick()
        composeRule.onNodeWithText("Under 65").performClick()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("No").performClick()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("Some days").performClick()
        composeRule.onAllNodesWithText("No")[0].performClick()
        composeRule.onAllNodesWithText("No")[1].performClick()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("No liver disease").performClick()
        composeRule.onNodeWithText("None").performClick()
        composeRule.onNodeWithText("See my profile").performClick()

        composeRule.onNodeWithText("Profile ready").assertIsDisplayed()
    }

    @Test
    fun introBrandAndTicker_renderOnCompactHeight() {
        setSizedContent(width = 360.dp, height = 640.dp) {
            IntroScreen(onStart = {}, onSkip = {})
        }

        composeRule.onNodeWithText("Caffeine Health").assertIsDisplayed()
        composeRule.onAllNodesWithText(
            "YOUR CAFFEINE PROFILE",
            substring = true,
            useUnmergedTree = true,
        )[0].assertIsDisplayed()
    }

    @Test
    fun introBrandAndTicker_renderOnRegularHeight() {
        setSizedContent(width = 412.dp, height = 915.dp) {
            IntroScreen(onStart = {}, onSkip = {})
        }

        composeRule.onNodeWithText("Caffeine Health").assertIsDisplayed()
        composeRule.onAllNodesWithText(
            "YOUR CAFFEINE PROFILE",
            substring = true,
            useUnmergedTree = true,
        )[0].assertIsDisplayed()
    }

    @Test
    fun gridChoices_showFullLabelsAndRemainSelectable() {
        setSizedContent(width = 412.dp, height = 915.dp) {
            TestGridVerificationHost()
        }

        composeRule.onNodeWithText("Some days").assertIsDisplayed()
        composeRule.onNodeWithText("Some days").performClick()
        composeRule.onNodeWithText("Continue").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("Oral contraceptives").assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithText(
                "Other CYP1A2 inhibitor",
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty(),
        )
        composeRule.onNodeWithText("Oral contraceptives").performClick()
        composeRule.onNodeWithText("1 selected").assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithText(
                "Oral contraceptives",
                useUnmergedTree = true,
            ).fetchSemanticsNodes().size >= 2,
        )
        composeRule.onNodeWithText("See my profile").assertIsDisplayed()
    }

    @Test
    fun medicalScreen_compactLayoutShowsSummaryAndSource() {
        setSizedContent(width = 360.dp, height = 640.dp) {
            MedicalScreen(
                uiState = OnboardingUiState(
                    answers = OnboardingAnswers(
                        ageBucket = AgeBucket.Under65,
                        hasInsomnia = false,
                        heavyAlcohol = false,
                        heavyCaffeine = false,
                        smokingHabit = SmokingHabit.None,
                        liverDisease = LiverDisease.None,
                    ),
                ),
                onBack = {},
                onSkip = {},
                onContinue = {},
                onLiverDiseaseChanged = {},
                onMedicationToggled = {},
            )
        }

        composeRule.onNodeWithText("None selected yet").assertIsDisplayed()
        composeRule.onNodeWithText("Oral contraceptives").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Some CYP1A2 inhibitors can make caffeine hang around much longer than usual.",
            substring = true,
        ).assertIsDisplayed()
    }

    private fun setSizedContent(
        width: androidx.compose.ui.unit.Dp,
        height: androidx.compose.ui.unit.Dp,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            CaffeineTheme(dynamicColor = false) {
                Box(modifier = Modifier.requiredSize(width = width, height = height)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun TestOnboardingFlowHost() {
    var destination by remember { mutableStateOf(OnboardingDestination.Intro) }
    var uiState by remember { mutableStateOf(OnboardingUiState()) }
    val displaySettings = remember { UserSettings() }

    fun updateAnswers(transform: (OnboardingAnswers) -> OnboardingAnswers) {
        uiState = uiState.copy(
            answers = transform(uiState.answers),
            useDefaultProfile = false,
        )
    }

    when (destination) {
        OnboardingDestination.Intro -> IntroScreen(
            onStart = { destination = OnboardingDestination.BasicInfo },
            onSkip = { destination = OnboardingDestination.ProfileReady },
        )

        OnboardingDestination.BasicInfo -> BasicInfoScreen(
            uiState = uiState,
            onBack = { destination = OnboardingDestination.Intro },
            onSkip = { destination = OnboardingDestination.ProfileReady },
            onContinue = { destination = OnboardingDestination.Sleep },
            onAgeBucketSelected = { ageBucket ->
                updateAnswers { answers -> answers.copy(ageBucket = ageBucket) }
            },
            onWeightIncrement = {
                updateAnswers { answers -> answers.adjustedWeight(delta = 1) }
            },
            onWeightDecrement = {
                updateAnswers { answers -> answers.adjustedWeight(delta = -1) }
            },
            onWeightUnitSelected = { weightUnit ->
                updateAnswers { answers ->
                    answers.copy(
                        weightValue = weightUnit.convertFrom(
                            value = answers.weightValue,
                            from = answers.weightUnit,
                        ),
                        weightUnit = weightUnit,
                    )
                }
            },
        )

        OnboardingDestination.Sleep -> SleepScreen(
            uiState = uiState,
            displaySettings = displaySettings,
            onBack = { destination = OnboardingDestination.BasicInfo },
            onSkip = { destination = OnboardingDestination.ProfileReady },
            onContinue = { destination = OnboardingDestination.Lifestyle },
            onSleepTimeChanged = { sleepTime ->
                updateAnswers { answers -> answers.copy(sleepTime = sleepTime) }
            },
            onInsomniaChanged = { hasInsomnia ->
                updateAnswers { answers -> answers.copy(hasInsomnia = hasInsomnia) }
            },
        )

        OnboardingDestination.Lifestyle -> LifestyleScreen(
            uiState = uiState,
            onBack = { destination = OnboardingDestination.Sleep },
            onSkip = { destination = OnboardingDestination.ProfileReady },
            onContinue = { destination = OnboardingDestination.Medical },
            onSmokingHabitChanged = { smokingHabit ->
                updateAnswers { answers -> answers.copy(smokingHabit = smokingHabit) }
            },
            onHeavyAlcoholChanged = { heavyAlcohol ->
                updateAnswers { answers -> answers.copy(heavyAlcohol = heavyAlcohol) }
            },
            onHeavyCaffeineChanged = { heavyCaffeine ->
                updateAnswers { answers -> answers.copy(heavyCaffeine = heavyCaffeine) }
            },
        )

        OnboardingDestination.Medical -> MedicalScreen(
            uiState = uiState,
            onBack = { destination = OnboardingDestination.Lifestyle },
            onSkip = { destination = OnboardingDestination.ProfileReady },
            onContinue = { destination = OnboardingDestination.ProfileReady },
            onLiverDiseaseChanged = { liverDisease ->
                updateAnswers { answers -> answers.copy(liverDisease = liverDisease) }
            },
            onMedicationToggled = { medication ->
                updateAnswers { answers -> answers.toggleMedication(medication) }
            },
        )

        OnboardingDestination.ProfileReady -> ProfileReadyScreen(
            uiState = uiState,
            displaySettings = displaySettings,
            onBack = { destination = OnboardingDestination.Medical },
            onOpenLegalSheet = {},
            onDismissLegalSheet = {},
            onLegalAcknowledgedChanged = {},
            onComplete = {},
        )
    }
}

@Composable
private fun TestGridVerificationHost() {
    var answers by remember {
        mutableStateOf(
            OnboardingAnswers(
                ageBucket = AgeBucket.Under65,
                hasInsomnia = false,
                heavyAlcohol = false,
                heavyCaffeine = false,
                liverDisease = LiverDisease.None,
            ),
        )
    }
    var stage by remember { mutableStateOf(0) }

    if (stage == 0) {
        LifestyleScreen(
            uiState = OnboardingUiState(answers = answers),
            onBack = {},
            onSkip = {},
            onContinue = { stage = 1 },
            onSmokingHabitChanged = { smokingHabit ->
                answers = answers.copy(smokingHabit = smokingHabit)
            },
            onHeavyAlcoholChanged = { heavyAlcohol ->
                answers = answers.copy(heavyAlcohol = heavyAlcohol)
            },
            onHeavyCaffeineChanged = { heavyCaffeine ->
                answers = answers.copy(heavyCaffeine = heavyCaffeine)
            },
        )
    } else {
        MedicalScreen(
            uiState = OnboardingUiState(answers = answers),
            onBack = { stage = 0 },
            onSkip = {},
            onContinue = {},
            onLiverDiseaseChanged = { liverDisease ->
                answers = answers.copy(liverDisease = liverDisease)
            },
            onMedicationToggled = { medication ->
                answers = answers.toggleMedication(medication)
            },
        )
    }
}
