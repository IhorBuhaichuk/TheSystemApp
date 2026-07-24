package com.ihor.thesystem.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.OnboardingAnswers
import com.ihor.thesystem.domain.model.OnboardingCyclePreset
import com.ihor.thesystem.domain.model.OnboardingExperience
import com.ihor.thesystem.domain.model.OnboardingGoal
import com.ihor.thesystem.domain.model.ValidationError
import com.ihor.thesystem.domain.usecase.CompleteOnboardingUseCase
import com.ihor.thesystem.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    NAME,
    GOAL,
    EQUIPMENT,
    EXPERIENCE,
    CYCLE
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.NAME,
    val name: String = "",
    val selectedGoal: OnboardingGoal = OnboardingGoal.BUILD_HABIT,
    val selectedEquipment: Set<EquipmentType> = setOf(EquipmentType.BODY_ONLY),
    val selectedExperience: OnboardingExperience = OnboardingExperience.BEGINNER,
    val selectedCyclePreset: OnboardingCyclePreset = OnboardingCyclePreset.FOUR_DAY,
    val isSaving: Boolean = false,
    val errorMessage: UiText? = null
) {
    val stepIndex: Int get() = STEPS.indexOf(step).coerceAtLeast(0)
    val totalSteps: Int get() = STEPS.size
    val progress: Float get() = ((stepIndex + 1).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    val canGoBack: Boolean get() = stepIndex > 0 && !isSaving
    val isLastStep: Boolean get() = stepIndex == totalSteps - 1
    val canContinue: Boolean get() = !isSaving && (step != OnboardingStep.NAME || name.isNotBlank())

    companion object {
        val STEPS: List<OnboardingStep> = OnboardingStep.values().toList()
    }
}

sealed interface OnboardingEvent {
    data object Completed : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(name = name.take(MAX_NAME_INPUT_LENGTH), errorMessage = null)
        }
    }

    fun onGoalSelected(goal: OnboardingGoal) {
        _uiState.update { it.copy(selectedGoal = goal, errorMessage = null) }
    }

    fun onEquipmentToggled(type: EquipmentType) {
        if (type == EquipmentType.BODY_ONLY) return
        _uiState.update { state ->
            val next = if (type in state.selectedEquipment) {
                state.selectedEquipment - type
            } else {
                state.selectedEquipment + type
            }
            state.copy(
                selectedEquipment = next + EquipmentType.BODY_ONLY,
                errorMessage = null
            )
        }
    }

    fun onExperienceSelected(experience: OnboardingExperience) {
        _uiState.update { it.copy(selectedExperience = experience, errorMessage = null) }
    }

    fun onCyclePresetSelected(preset: OnboardingCyclePreset) {
        _uiState.update { it.copy(selectedCyclePreset = preset, errorMessage = null) }
    }

    fun onBack() {
        _uiState.update { state ->
            val previousIndex = (state.stepIndex - 1).coerceAtLeast(0)
            state.copy(step = OnboardingUiState.STEPS[previousIndex], errorMessage = null)
        }
    }

    fun onContinue() {
        val state = _uiState.value
        if (!state.canContinue) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Введи ім'я, щоб система могла створити профіль."))
            }
            return
        }

        if (!state.isLastStep) {
            _uiState.update {
                it.copy(
                    step = OnboardingUiState.STEPS[it.stepIndex + 1],
                    errorMessage = null
                )
            }
            return
        }

        complete(state)
    }

    private fun complete(state: OnboardingUiState) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = completeOnboarding(
                OnboardingAnswers(
                    name = state.name,
                    goal = state.selectedGoal,
                    equipment = state.selectedEquipment,
                    experience = state.selectedExperience,
                    cyclePreset = state.selectedCyclePreset
                )
            )
            when (result) {
                is Result.Success -> _events.emit(OnboardingEvent.Completed)
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.toUiText())
                }
            }
        }
    }

    private fun DomainError.toUiText(): UiText =
        when (this) {
            ValidationError.INVALID_PLAYER_NAME ->
                UiText.DynamicString("Ім'я має бути від 1 до 50 символів.")
            else ->
                UiText.DynamicString(message ?: "Не вдалося завершити onboarding.")
        }

    private companion object {
        const val MAX_NAME_INPUT_LENGTH = 50
    }
}
