package com.uc.caffeine.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.uc.caffeine.R
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.util.formatTimeOfDay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IntroScreen(
    onStart: () -> Unit,
    onSkip: () -> Unit,
) {
    OnboardingScaffold(
        title = "Let’s tune your caffeine rhythm",
        subtitle = "A better first estimate makes the chart and bedtime guidance feel personal right away.",
        currentStep = 0,
        showBackButton = false,
        showSkipButton = true,
        onSkip = onSkip,
        continueLabel = "Start profiling",
        continueEnabled = true,
        onContinue = onStart,
        enabledHint = "Usually takes about a minute.",
        pushCardUpward = true,
        leadingHeaderContent = { layout ->
            OnboardingBrandLockup(layout = layout)
        },
        heroContent = { layout ->
            OnboardingHero(layout = layout)
            OnboardingMarquee(text = "Your caffeine profile")
        },
    ) {
        TypingText(
            text = "We’ll set a bedtime-aware starting profile so your first cup feels believable instead of generic.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OnboardingInfoBadge(label = "Live curve")
            OnboardingInfoBadge(label = "Bedtime forecast")
            OnboardingInfoBadge(label = "Sleep threshold")
        }

        Text(
            text = buildAnnotatedString {
                append("You can still fine-tune ")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                ) {
                    append("half-life, bedtime, and threshold")
                }
                append(" later in Settings.")
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun BasicInfoScreen(
    uiState: OnboardingUiState,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onAgeBucketSelected: (AgeBucket) -> Unit,
    onWeightIncrement: () -> Unit,
    onWeightDecrement: () -> Unit,
    onWeightUnitSelected: (WeightUnit) -> Unit,
    onWeightChanged: (Int) -> Unit = {},
) {
    OnboardingScaffold(
        title = "Set your baseline",
        subtitle = "Pick your age range and leave the default weight alone unless you want to tune it now.",
        currentStep = OnboardingDestination.BasicInfo.stepNumber,
        showBackButton = true,
        onBack = onBack,
        showSkipButton = true,
        onSkip = onSkip,
        continueLabel = "Continue",
        continueEnabled = uiState.answers.isBasicInfoComplete(),
        onContinue = onContinue,
        disabledHint = "Choose your age range to continue.",
        enabledHint = "Weight starts at 60 kg and can be adjusted later.",
    ) {
        OnboardingSection(
            title = "Age range",
            supportingText = "This keeps the estimate conservative when caffeine may linger longer.",
        ) {
            ConnectedChoiceButtonGroup(
                options = AgeBucket.entries,
                selectedOption = uiState.answers.ageBucket,
                labelFor = { ageBucket -> ageBucket.label },
                onOptionSelected = onAgeBucketSelected,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

        OnboardingSection(
            title = "Weight",
            supportingText = "Use the stepper if you want to adjust the default starting weight.",
        ) {
            WeightStepperCard(
                weightValue = uiState.answers.weightValue,
                weightUnit = uiState.answers.weightUnit,
                onWeightUnitSelected = onWeightUnitSelected,
                onIncrement = onWeightIncrement,
                onDecrement = onWeightDecrement,
                onWeightChanged = onWeightChanged,
            )
        }
    }
}

@Composable
internal fun SleepScreen(
    uiState: OnboardingUiState,
    displaySettings: UserSettings,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onSleepTimeChanged: (java.time.LocalTime) -> Unit,
    onInsomniaChanged: (Boolean) -> Unit,
) {
    OnboardingScaffold(
        title = "Map your sleep window",
        subtitle = "Bedtime starts at 11 PM, so you only need to change it if your usual night looks different.",
        currentStep = OnboardingDestination.Sleep.stepNumber,
        showBackButton = true,
        onBack = onBack,
        showSkipButton = true,
        onSkip = onSkip,
        continueLabel = "Continue",
        continueEnabled = uiState.answers.isSleepComplete(),
        onContinue = onContinue,
        disabledHint = "Tell us if sleep is sensitive for you.",
        enabledHint = "This powers the bedtime forecast and threshold line.",
    ) {
        SleepTimePickerCard(
            displaySettings = displaySettings,
            selectedTime = uiState.answers.sleepTime,
            onSleepTimeChanged = onSleepTimeChanged,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

        OnboardingSection(
            title = "Sleep sensitivity",
            supportingText = "If insomnia is already part of the picture, we keep the bedtime target more conservative.",
        ) {
            ConnectedChoiceButtonGroup(
                options = listOf(true, false),
                selectedOption = uiState.answers.hasInsomnia,
                labelFor = { if (it) "Yes" else "No" },
                onOptionSelected = onInsomniaChanged,
            )
        }
    }
}

@Composable
internal fun LifestyleScreen(
    uiState: OnboardingUiState,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onSmokingHabitChanged: (SmokingHabit) -> Unit,
    onHeavyAlcoholChanged: (Boolean) -> Unit,
    onHeavyCaffeineChanged: (Boolean) -> Unit,
) {
    OnboardingScaffold(
        title = "Add the real-life context",
        subtitle = "These habits change how quickly caffeine clears and how careful bedtime guidance should stay.",
        currentStep = OnboardingDestination.Lifestyle.stepNumber,
        showBackButton = true,
        onBack = onBack,
        showSkipButton = true,
        onSkip = onSkip,
        continueLabel = "Continue",
        continueEnabled = uiState.answers.isLifestyleComplete(),
        onContinue = onContinue,
        disabledHint = "Tell us about smoking, alcohol, and daily caffeine.",
        enabledHint = "We blend these with your baseline before the final profile.",
    ) {
        OnboardingSection(title = "Smoking habit") {
            GridSingleSelectButtonGroup(
                options = SmokingHabit.entries,
                selectedOption = uiState.answers.smokingHabit,
                labelFor = { smokingHabit -> smokingHabit.buttonLabel() },
                onOptionSelected = onSmokingHabitChanged,
            )
        }

        OnboardingSection(title = "Heavy alcohol use") {
            ConnectedChoiceButtonGroup(
                options = listOf(true, false),
                selectedOption = uiState.answers.heavyAlcohol,
                labelFor = { if (it) "Yes" else "No" },
                onOptionSelected = onHeavyAlcoholChanged,
            )
        }

        OnboardingSection(title = "High daily caffeine") {
            ConnectedChoiceButtonGroup(
                options = listOf(true, false),
                selectedOption = uiState.answers.heavyCaffeine,
                labelFor = { if (it) "Yes" else "No" },
                onOptionSelected = onHeavyCaffeineChanged,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

        InfoTextWithSource(
            text = "Smoking can speed up caffeine clearance for some people by affecting CYP1A2 activity.",
            sourceUrl = stringResource(R.string.onboarding_smoking_source_url),
        )
        InfoTextWithSource(
            text = "Heavy alcohol use can slow clearance and lower your effective bedtime threshold.",
            sourceUrl = stringResource(R.string.onboarding_alcohol_source_url),
        )
        InfoTextWithSource(
            text = "Frequent high intake may increase tolerance even when bedtime guidance should still stay conservative.",
            sourceUrl = stringResource(R.string.onboarding_caffeine_source_url),
        )
    }
}

@Composable
internal fun MedicalScreen(
    uiState: OnboardingUiState,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onLiverDiseaseChanged: (LiverDisease) -> Unit,
    onMedicationToggled: (Medication) -> Unit,
) {
    val medicationSummary = when {
        uiState.answers.medications.isEmpty() -> "None selected yet"
        uiState.answers.medications == setOf(Medication.None) -> "None selected"
        else -> "${uiState.answers.medications.size} selected"
    }

    OnboardingScaffold(
        title = "Keep the estimate responsible",
        subtitle = "A little health context keeps the first-day profile from being overly optimistic.",
        currentStep = OnboardingDestination.Medical.stepNumber,
        showBackButton = true,
        onBack = onBack,
        showSkipButton = true,
        onSkip = onSkip,
        continueLabel = "See my profile",
        continueEnabled = uiState.answers.isMedicalComplete(),
        onContinue = onContinue,
        disabledHint = "Pick liver context and any medicines that matter here.",
        enabledHint = "This only sets a safer starting profile.",
    ) {
        OnboardingSection(title = "Liver context") {
            GridSingleSelectButtonGroup(
                options = LiverDisease.entries,
                selectedOption = uiState.answers.liverDisease,
                labelFor = { liverDisease -> liverDisease.buttonLabel() },
                onOptionSelected = onLiverDiseaseChanged,
            )
        }

        OnboardingSection(
            title = "Medicines that may slow caffeine clearance",
            supportingText = "Pick any that apply. If none do, select None.",
        ) {
            GridMultiSelectButtonGroup(
                options = Medication.entries,
                selectedOptions = uiState.answers.medications,
                labelFor = { medication -> medication.buttonLabel() },
                onOptionToggled = onMedicationToggled,
            )
            SelectedSummary(
                label = "Selected",
                value = medicationSummary,
            )
            InfoTextWithSource(
                text = "Some CYP1A2 inhibitors can make caffeine hang around much longer than usual.",
                sourceUrl = stringResource(R.string.onboarding_medication_source_url),
            )
        }
    }
}

@Composable
internal fun ProfileReadyScreen(
    uiState: OnboardingUiState,
    displaySettings: UserSettings,
    onBack: () -> Unit,
    onOpenLegalSheet: () -> Unit,
    onDismissLegalSheet: () -> Unit,
    onLegalAcknowledgedChanged: (Boolean) -> Unit,
    onComplete: () -> Unit,
) {
    val profile = uiState.currentProfile() ?: OnboardingProfileCalculator.defaultProfile
    var infoDialog by rememberSaveable { mutableStateOf<ProfileInfoDialog?>(null) }

    OnboardingScaffold(
        title = "Profile ready",
        subtitle = "Here’s the starting profile Caffeine will use for your live curve and bedtime forecast.",
        currentStep = OnboardingDestination.ProfileReady.stepNumber,
        showBackButton = true,
        onBack = onBack,
        showSkipButton = false,
        continueLabel = "Review legal bits",
        continueEnabled = true,
        onContinue = onOpenLegalSheet,
        enabledHint = "You can fine-tune these values later in Settings.",
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Dialed in for your first day",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "This gives you a more believable chart and bedtime forecast from the very first drink you log.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.86f),
                )
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileMetricRow(
                    title = "Estimated half-life",
                    value = formatHalfLife(profile.halfLifeMinutes),
                    description = "How long it takes your body to clear half of the caffeine you consume.",
                    onInfoClick = { infoDialog = ProfileInfoDialog.HalfLife },
                )
                HorizontalDivider()
                ProfileMetricRow(
                    title = "Sleep threshold",
                    value = "${profile.sleepThresholdMg} mg",
                    description = "A conservative bedtime target for how much active caffeine may still be in your system.",
                    onInfoClick = { infoDialog = ProfileInfoDialog.SleepThreshold },
                )
                HorizontalDivider()
                ProfileMetricRow(
                    title = "Typical bedtime",
                    value = formatTimeOfDay(
                        hour = profile.sleepTimeHour,
                        minute = profile.sleepTimeMinute,
                        settings = displaySettings,
                    ),
                    description = "Used to project the sleep forecast card and chart markers each day.",
                    onInfoClick = { infoDialog = ProfileInfoDialog.Bedtime },
                )
            }
        }
    }

    if (uiState.showLegalSheet) {
        LegalSheet(
            legalAcknowledged = uiState.legalAcknowledged,
            isSaving = uiState.isSaving,
            onDismiss = onDismissLegalSheet,
            onAcknowledgedChanged = onLegalAcknowledgedChanged,
            onComplete = onComplete,
        )
    }

    infoDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) {
                    Text("Close")
                }
            },
            title = {
                Text(dialog.title)
            },
            text = {
                Text(dialog.body)
            },
        )
    }
}

internal enum class ProfileInfoDialog(
    val title: String,
    val body: String,
) {
    HalfLife(
        title = "Estimated half-life",
        body = "Half-life is the estimated time it takes your body to clear half of the caffeine still active in your system. A longer half-life means caffeine may linger later into the day.",
    ),
    SleepThreshold(
        title = "Sleep threshold",
        body = "This is a conservative bedtime target for how much active caffeine may still be in your system before sleep is more likely to be affected.",
    ),
    Bedtime(
        title = "Typical bedtime",
        body = "Bedtime powers the chart marker and the sleep forecast card so the app can estimate how much caffeine may still be active when you try to sleep.",
    ),
}
