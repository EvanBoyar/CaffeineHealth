package com.uc.caffeine.ui.onboarding

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStateTest {

    @Test
    fun toggleMedication_noneClearsOtherSelections() {
        val answers = OnboardingAnswers(
            medications = setOf(Medication.Cimetidine, Medication.Ciprofloxacin),
        )

        val updated = answers.toggleMedication(Medication.None)

        assertEquals(setOf(Medication.None), updated.medications)
    }

    @Test
    fun toggleMedication_selectingMedicationRemovesNone() {
        val answers = OnboardingAnswers(
            medications = setOf(Medication.None),
        )

        val updated = answers.toggleMedication(Medication.Cimetidine)

        assertEquals(setOf(Medication.Cimetidine), updated.medications)
    }

    @Test
    fun validationHelpers_trackStepCompletion() {
        val incomplete = OnboardingAnswers()
        assertFalse(incomplete.isProfileReady())
        assertFalse(incomplete.isBasicInfoComplete())
        assertFalse(incomplete.isSleepComplete())

        val prefilledAnswers = OnboardingAnswers(
            ageBucket = AgeBucket.Under65,
            hasInsomnia = false,
        )

        assertTrue(prefilledAnswers.isBasicInfoComplete())
        assertTrue(prefilledAnswers.isSleepComplete())

        val complete = OnboardingAnswers(
            ageBucket = AgeBucket.Under65,
            weightValue = 72,
            weightUnit = WeightUnit.Kilograms,
            hasInsomnia = false,
            smokingHabit = SmokingHabit.None,
            heavyAlcohol = false,
            heavyCaffeine = false,
            liverDisease = LiverDisease.None,
            medications = setOf(Medication.None),
        )

        assertTrue(complete.isBasicInfoComplete())
        assertTrue(complete.isSleepComplete())
        assertTrue(complete.isLifestyleComplete())
        assertTrue(complete.isMedicalComplete())
        assertTrue(complete.isProfileReady())
    }

    @Test
    fun weightConversionAndClamping_followCurrentUnitRules() {
        val answers = OnboardingAnswers(weightValue = 60, weightUnit = WeightUnit.Kilograms)

        val convertedToPounds = WeightUnit.Pounds.convertFrom(
            value = answers.weightValue,
            from = answers.weightUnit,
        )
        val clampedKilograms = answers.copy(weightValue = 30).adjustedWeight(delta = -5)

        assertEquals(132, convertedToPounds)
        assertEquals(30, clampedKilograms.weightValue)
    }
}
