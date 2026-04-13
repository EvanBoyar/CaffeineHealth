package com.uc.caffeine.ui.onboarding

import androidx.navigation3.runtime.NavKey
import com.uc.caffeine.data.DerivedOnboardingProfile
import java.time.LocalTime
import kotlin.math.roundToInt

enum class OnboardingDestination(
    val stepNumber: Int,
) : NavKey {
    Intro(0),
    BasicInfo(1),
    Sleep(2),
    Lifestyle(3),
    Medical(4),
    ProfileReady(5),
}

enum class AgeBucket(val label: String) {
    Under65("Under 65"),
    Over65("65 or older"),
}

enum class WeightUnit(val label: String) {
    Kilograms("kg"),
    Pounds("lb"),
    ;

    fun clamp(value: Int): Int = value.coerceIn(minSelectable(), maxSelectable())

    fun convertFrom(value: Int, from: WeightUnit): Int {
        if (this == from) return clamp(value)
        return when (this) {
            Kilograms -> (value / PoundsPerKilogram).roundToInt()
            Pounds -> (value * PoundsPerKilogram).roundToInt()
        }.let(::clamp)
    }

    fun minSelectable(): Int {
        return when (this) {
            Kilograms -> 30
            Pounds -> (30 * PoundsPerKilogram).roundToInt()
        }
    }

    fun maxSelectable(): Int {
        return when (this) {
            Kilograms -> 220
            Pounds -> (220 * PoundsPerKilogram).roundToInt()
        }
    }

    companion object {
        private const val PoundsPerKilogram = 2.2046226218
    }
}

enum class SmokingHabit(val label: String) {
    None("None"),
    Occasional("Occasional"),
    Daily("Daily"),
    Heavy("Heavy"),
}

enum class LiverDisease(val label: String) {
    None("No liver disease"),
    Compensated("Compensated"),
    Decompensated("Decompensated"),
}

enum class Medication(
    val label: String,
    val halfLifeDeltaMinutes: Int,
) {
    None("None", 0),
    Cimetidine("Cimetidine", 45),
    OralContraceptives("Oral contraceptives", 60),
    Ciprofloxacin("Ciprofloxacin", 120),
    Fluvoxamine("Fluvoxamine", 180),
    OtherCyp1A2Inhibitor("Other CYP1A2 inhibitor", 60),
}

data class OnboardingAnswers(
    val ageBucket: AgeBucket? = null,
    val weightValue: Int = 60,
    val weightUnit: WeightUnit = WeightUnit.Kilograms,
    val sleepTime: LocalTime = LocalTime.of(23, 0),
    val hasInsomnia: Boolean? = null,
    val smokingHabit: SmokingHabit? = null,
    val heavyAlcohol: Boolean? = null,
    val heavyCaffeine: Boolean? = null,
    val liverDisease: LiverDisease? = null,
    val medications: Set<Medication> = emptySet(),
) {
    fun weightKg(): Double {
        val weight = weightUnit.clamp(weightValue).toDouble()
        return when (weightUnit) {
            WeightUnit.Kilograms -> weight
            WeightUnit.Pounds -> weight / 2.2046226218
        }
    }

    fun isBasicInfoComplete(): Boolean = ageBucket != null

    fun isSleepComplete(): Boolean = hasInsomnia != null

    fun isLifestyleComplete(): Boolean {
        return smokingHabit != null && heavyAlcohol != null && heavyCaffeine != null
    }

    fun isMedicalComplete(): Boolean = liverDisease != null && medications.isNotEmpty()

    fun isProfileReady(): Boolean {
        return isBasicInfoComplete() && isSleepComplete() && isLifestyleComplete() && isMedicalComplete()
    }

    fun adjustedWeight(delta: Int): OnboardingAnswers {
        return copy(weightValue = weightUnit.clamp(weightValue + delta))
    }

    fun toggleMedication(medication: Medication): OnboardingAnswers {
        val nextMedications = medications.toMutableSet()

        when (medication) {
            Medication.None -> {
                if (medications == setOf(Medication.None)) {
                    nextMedications.clear()
                } else {
                    nextMedications.clear()
                    nextMedications.add(Medication.None)
                }
            }

            else -> {
                nextMedications.remove(Medication.None)
                if (nextMedications.contains(medication)) {
                    nextMedications.remove(medication)
                } else {
                    nextMedications.add(medication)
                }
            }
        }

        return copy(medications = nextMedications)
    }
}

data class OnboardingUiState(
    val answers: OnboardingAnswers = OnboardingAnswers(),
    val useDefaultProfile: Boolean = false,
    val showLegalSheet: Boolean = false,
    val legalAcknowledged: Boolean = false,
    val isSaving: Boolean = false,
    val isCelebrating: Boolean = false,
) {
    fun currentProfile(): DerivedOnboardingProfile? {
        return when {
            useDefaultProfile -> OnboardingProfileCalculator.defaultProfile
            answers.isProfileReady() -> OnboardingProfileCalculator.calculateProfile(answers)
            else -> null
        }
    }

    fun canContinue(destination: OnboardingDestination): Boolean {
        return when (destination) {
            OnboardingDestination.Intro -> true
            OnboardingDestination.BasicInfo -> answers.isBasicInfoComplete()
            OnboardingDestination.Sleep -> answers.isSleepComplete()
            OnboardingDestination.Lifestyle -> answers.isLifestyleComplete()
            OnboardingDestination.Medical -> answers.isMedicalComplete()
            OnboardingDestination.ProfileReady -> currentProfile() != null && legalAcknowledged && !isSaving
        }
    }
}

internal fun SmokingHabit.buttonLabel(): String {
    return when (this) {
        SmokingHabit.Occasional -> "Some days"
        else -> label
    }
}

internal fun LiverDisease.buttonLabel(): String {
    return when (this) {
        LiverDisease.None -> "No liver disease"
        LiverDisease.Compensated -> "Compensated"
        LiverDisease.Decompensated -> "Decompensated"
    }
}

internal fun Medication.buttonLabel(): String {
    return when (this) {
        Medication.OralContraceptives -> "Oral contraceptives"
        Medication.OtherCyp1A2Inhibitor -> "Other CYP1A2 inhibitor"
        else -> label
    }
}
