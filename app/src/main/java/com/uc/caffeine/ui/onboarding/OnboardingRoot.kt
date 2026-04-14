package com.uc.caffeine.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.uc.caffeine.data.UserSettings

@Composable
fun OnboardingRoot(
    displaySettings: UserSettings,
    modifier: Modifier = Modifier,
    resetSessionToken: Int? = null,
    onExit: (() -> Unit)? = null,
    onFinished: (() -> Unit)? = null,
    viewModel: OnboardingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(OnboardingDestination.Intro)

    LaunchedEffect(resetSessionToken) {
        if (resetSessionToken != null) {
            viewModel.resetSession()
        }
    }

    fun navigateTo(destination: OnboardingDestination) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                } else {
                    onExit?.invoke()
                }
            },
            modifier = Modifier.fillMaxSize(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
            ),
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                    (slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
            },
            popTransitionSpec = {
                (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = entryProvider {
                entry<OnboardingDestination> { destination ->
                    when (destination) {
                        OnboardingDestination.Intro -> IntroScreen(
                            onStart = {
                                viewModel.startQuestionnaire()
                                navigateTo(OnboardingDestination.BasicInfo)
                            },
                            onSkip = {
                                viewModel.skipWithDefaultProfile()
                                navigateTo(OnboardingDestination.ProfileReady)
                            },
                        )

                        OnboardingDestination.BasicInfo -> BasicInfoScreen(
                            uiState = uiState,
                            onBack = { backStack.removeLastOrNull() },
                            onSkip = {
                                viewModel.skipWithDefaultProfile()
                                navigateTo(OnboardingDestination.ProfileReady)
                            },
                            onContinue = { navigateTo(OnboardingDestination.Sleep) },
                            onAgeBucketSelected = viewModel::updateAgeBucket,
                            onWeightIncrement = viewModel::incrementWeight,
                            onWeightDecrement = viewModel::decrementWeight,
                            onWeightUnitSelected = viewModel::updateWeightUnit,
                            onWeightChanged = viewModel::setWeight,
                        )

                        OnboardingDestination.Sleep -> SleepScreen(
                            uiState = uiState,
                            displaySettings = displaySettings,
                            onBack = { backStack.removeLastOrNull() },
                            onSkip = {
                                viewModel.skipWithDefaultProfile()
                                navigateTo(OnboardingDestination.ProfileReady)
                            },
                            onContinue = { navigateTo(OnboardingDestination.Lifestyle) },
                            onSleepTimeChanged = viewModel::updateSleepTime,
                            onInsomniaChanged = viewModel::updateInsomnia,
                        )

                        OnboardingDestination.Lifestyle -> LifestyleScreen(
                            uiState = uiState,
                            onBack = { backStack.removeLastOrNull() },
                            onSkip = {
                                viewModel.skipWithDefaultProfile()
                                navigateTo(OnboardingDestination.ProfileReady)
                            },
                            onContinue = { navigateTo(OnboardingDestination.Medical) },
                            onSmokingHabitChanged = viewModel::updateSmokingHabit,
                            onHeavyAlcoholChanged = viewModel::updateHeavyAlcohol,
                            onHeavyCaffeineChanged = viewModel::updateHeavyCaffeine,
                        )

                        OnboardingDestination.Medical -> MedicalScreen(
                            uiState = uiState,
                            onBack = { backStack.removeLastOrNull() },
                            onSkip = {
                                viewModel.skipWithDefaultProfile()
                                navigateTo(OnboardingDestination.ProfileReady)
                            },
                            onContinue = { navigateTo(OnboardingDestination.ProfileReady) },
                            onLiverDiseaseChanged = viewModel::updateLiverDisease,
                            onMedicationToggled = viewModel::toggleMedication,
                        )

                        OnboardingDestination.ProfileReady -> ProfileReadyScreen(
                            uiState = uiState,
                            displaySettings = displaySettings,
                            onBack = { backStack.removeLastOrNull() },
                            onOpenLegalSheet = viewModel::showLegalSheet,
                            onDismissLegalSheet = viewModel::hideLegalSheet,
                            onLegalAcknowledgedChanged = viewModel::setLegalAcknowledged,
                            onComplete = {
                                viewModel.startCelebration()
                            },
                        )
                    }
                }
            },
        )

            if (uiState.isCelebrating) {
                BackHandler {}

                CompletionCelebration(
                    onComplete = {
                        viewModel.completeOnboarding {
                            onFinished?.invoke()
                        }
                    },
                )
            }
        }
    }
}
