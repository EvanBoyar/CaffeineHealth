package com.uc.caffeine.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uc.caffeine.data.ProfileFactors
import com.uc.caffeine.data.SettingsRepository
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun resetSession() {
        _uiState.value = OnboardingUiState()
    }

    fun startQuestionnaire() {
        _uiState.update { state ->
            state.copy(
                useDefaultProfile = false,
                showLegalSheet = false,
                legalAcknowledged = false,
            )
        }
    }

    fun skipWithDefaultProfile() {
        _uiState.update { state ->
            state.copy(
                useDefaultProfile = true,
                showLegalSheet = false,
                legalAcknowledged = false,
            )
        }
    }

    fun updateAgeBucket(ageBucket: AgeBucket) {
        updateAnswers { answers -> answers.copy(ageBucket = ageBucket) }
    }

    fun incrementWeight() {
        updateAnswers { answers -> answers.adjustedWeight(delta = 1) }
    }

    fun decrementWeight() {
        updateAnswers { answers -> answers.adjustedWeight(delta = -1) }
    }

    fun setWeight(value: Int) {
        updateAnswers { answers -> answers.copy(weightValue = answers.weightUnit.clamp(value)) }
    }

    fun updateWeightUnit(weightUnit: WeightUnit) {
        updateAnswers { answers ->
            val convertedWeight = weightUnit.convertFrom(
                value = answers.weightValue,
                from = answers.weightUnit,
            )
            answers.copy(
                weightValue = convertedWeight,
                weightUnit = weightUnit,
            )
        }
    }

    fun updateSleepTime(sleepTime: LocalTime) {
        updateAnswers { answers -> answers.copy(sleepTime = sleepTime) }
    }

    fun updateInsomnia(hasInsomnia: Boolean) {
        updateAnswers { answers -> answers.copy(hasInsomnia = hasInsomnia) }
    }

    fun updateSmokingHabit(smokingHabit: SmokingHabit) {
        updateAnswers { answers -> answers.copy(smokingHabit = smokingHabit) }
    }

    fun updateHeavyAlcohol(heavyAlcohol: Boolean) {
        updateAnswers { answers -> answers.copy(heavyAlcohol = heavyAlcohol) }
    }

    fun updateHeavyCaffeine(heavyCaffeine: Boolean) {
        updateAnswers { answers -> answers.copy(heavyCaffeine = heavyCaffeine) }
    }

    fun updateLiverDisease(liverDisease: LiverDisease) {
        updateAnswers { answers -> answers.copy(liverDisease = liverDisease) }
    }

    fun toggleMedication(medication: Medication) {
        updateAnswers { answers -> answers.toggleMedication(medication) }
    }

    fun showLegalSheet() {
        if (_uiState.value.currentProfile() == null) return
        _uiState.update { state -> state.copy(showLegalSheet = true) }
    }

    fun hideLegalSheet() {
        _uiState.update { state ->
            if (state.isSaving) state else state.copy(showLegalSheet = false)
        }
    }

    fun setLegalAcknowledged(legalAcknowledged: Boolean) {
        _uiState.update { state -> state.copy(legalAcknowledged = legalAcknowledged) }
    }

    fun startCelebration() {
        val state = _uiState.value
        if (state.isCelebrating) return
        if (state.currentProfile() == null || !state.legalAcknowledged || state.isSaving) return
        _uiState.update { it.copy(isCelebrating = true, showLegalSheet = false) }
    }

    fun completeOnboarding(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val profile = state.currentProfile() ?: return
        if (!state.legalAcknowledged || state.isSaving) return

        val factors = if (!state.useDefaultProfile) {
            state.answers.toProfileFactors()
        } else null

        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isSaving = true) }
            runCatching {
                settingsRepository.completeOnboarding(profile, factors)
            }.onSuccess {
                _uiState.update { currentState ->
                    currentState.copy(
                        showLegalSheet = false,
                        isSaving = false,
                    )
                }
                onSuccess()
            }.onFailure {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        isCelebrating = false,
                        showLegalSheet = true,
                    )
                }
            }
        }
    }

    private fun updateAnswers(transform: (OnboardingAnswers) -> OnboardingAnswers) {
        _uiState.update { state ->
            state.copy(
                answers = transform(state.answers),
                useDefaultProfile = false,
            )
        }
    }
}

private fun OnboardingAnswers.toProfileFactors(): ProfileFactors = ProfileFactors(
    ageBucket = ageBucket?.name,
    weightValue = weightValue,
    weightUnit = weightUnit.name,
    hasInsomnia = hasInsomnia,
    smokingHabit = smokingHabit?.name,
    heavyAlcohol = heavyAlcohol,
    heavyCaffeine = heavyCaffeine,
    liverDisease = liverDisease?.name,
    medications = medications.map { it.name }.toSet(),
)
