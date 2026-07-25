package com.ihor.thesystem.feature.onboarding.ui

import androidx.activity.compose.ReportDrawn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemPanel
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.OnboardingCyclePreset
import com.ihor.thesystem.domain.model.OnboardingExperience
import com.ihor.thesystem.domain.model.OnboardingGoal
import com.ihor.thesystem.feature.onboarding.viewmodel.OnboardingEvent
import com.ihor.thesystem.feature.onboarding.viewmodel.OnboardingStep
import com.ihor.thesystem.feature.onboarding.viewmodel.OnboardingUiState
import com.ihor.thesystem.feature.onboarding.viewmodel.OnboardingViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors

    ReportDrawn()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is OnboardingEvent.Completed) {
                onCompleted()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            OnboardingHeader(uiState = uiState)
            OnboardingStepPanel(
                uiState = uiState,
                onNameChanged = viewModel::onNameChanged,
                onGoalSelected = viewModel::onGoalSelected,
                onEquipmentToggled = viewModel::onEquipmentToggled,
                onExperienceSelected = viewModel::onExperienceSelected,
                onCyclePresetSelected = viewModel::onCyclePresetSelected
            )
            OnboardingActions(
                uiState = uiState,
                onBack = viewModel::onBack,
                onContinue = viewModel::onContinue
            )
        }
    }
}

@Composable
private fun OnboardingHeader(uiState: OnboardingUiState) {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Система: новий рівень",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Початкове налаштування",
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        SystemProgressBar(
            progress = uiState.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun OnboardingStepPanel(
    uiState: OnboardingUiState,
    onNameChanged: (String) -> Unit,
    onGoalSelected: (OnboardingGoal) -> Unit,
    onEquipmentToggled: (EquipmentType) -> Unit,
    onExperienceSelected: (OnboardingExperience) -> Unit,
    onCyclePresetSelected: (OnboardingCyclePreset) -> Unit
) {
    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        active = true,
        accent = uiState.step.accent()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = uiState.step.icon(),
                    contentDescription = null,
                    tint = uiState.step.accent()
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.step.title(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = SystemTheme.colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Крок ${uiState.stepIndex + 1} / ${uiState.totalSteps}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SystemTheme.colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            when (uiState.step) {
                OnboardingStep.NAME -> NameStep(
                    name = uiState.name,
                    onNameChanged = onNameChanged
                )
                OnboardingStep.GOAL -> GoalStep(
                    selected = uiState.selectedGoal,
                    onSelected = onGoalSelected
                )
                OnboardingStep.EQUIPMENT -> EquipmentStep(
                    selected = uiState.selectedEquipment,
                    onToggled = onEquipmentToggled
                )
                OnboardingStep.EXPERIENCE -> ExperienceStep(
                    selected = uiState.selectedExperience,
                    onSelected = onExperienceSelected
                )
                OnboardingStep.CYCLE -> CycleStep(
                    selected = uiState.selectedCyclePreset,
                    onSelected = onCyclePresetSelected
                )
            }

            uiState.errorMessage?.let { error ->
                ErrorText(error)
            }
        }
    }
}

@Composable
private fun NameStep(
    name: String,
    onNameChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Ім'я") },
        colors = systemOutlinedTextFieldColors(accent = SystemTheme.colors.accentPrimary)
    )
}

@Composable
private fun GoalStep(
    selected: OnboardingGoal,
    onSelected: (OnboardingGoal) -> Unit
) {
    OptionList {
        OnboardingGoal.values().forEach { goal ->
            SelectablePlate(
                title = goal.title(),
                subtitle = goal.subtitle(),
                selected = goal == selected,
                accent = goal.accent(),
                onClick = { onSelected(goal) }
            )
        }
    }
}

@Composable
private fun EquipmentStep(
    selected: Set<EquipmentType>,
    onToggled: (EquipmentType) -> Unit
) {
    OptionList {
        EQUIPMENT_OPTIONS.forEach { option ->
            SelectablePlate(
                title = option.title,
                subtitle = option.subtitle,
                selected = option.type in selected,
                accent = SystemTheme.colors.accentSuccess,
                onClick = { onToggled(option.type) }
            )
        }
    }
}

@Composable
private fun ExperienceStep(
    selected: OnboardingExperience,
    onSelected: (OnboardingExperience) -> Unit
) {
    OptionList {
        OnboardingExperience.values().forEach { experience ->
            SelectablePlate(
                title = experience.title(),
                subtitle = experience.subtitle(),
                selected = experience == selected,
                accent = SystemTheme.colors.accentAi,
                onClick = { onSelected(experience) }
            )
        }
    }
}

@Composable
private fun CycleStep(
    selected: OnboardingCyclePreset,
    onSelected: (OnboardingCyclePreset) -> Unit
) {
    OptionList {
        OnboardingCyclePreset.values().forEach { preset ->
            SelectablePlate(
                title = preset.title(),
                subtitle = preset.subtitle(),
                selected = preset == selected,
                accent = SystemTheme.colors.accentWarning,
                onClick = { onSelected(preset) }
            )
        }
    }
}

@Composable
private fun OptionList(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun SelectablePlate(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .techSurface(
                shape = shape,
                active = selected,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .systemClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.Check else Icons.Filled.Settings,
            contentDescription = null,
            tint = if (selected) accent else colors.textMuted
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OnboardingActions(
    uiState: OnboardingUiState,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.canGoBack) {
            SystemButton(
                text = "Назад",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        SystemButton(
            text = if (uiState.isLastStep) "Перейти до плану" else "Далі",
            onClick = onContinue,
            modifier = Modifier.weight(1.35f),
            icon = if (uiState.isLastStep) Icons.Filled.Check else Icons.Filled.Whatshot,
            enabled = uiState.canContinue,
            glow = uiState.isLastStep
        )
    }

    if (uiState.isSaving) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SystemTheme.colors.accentPrimary)
        }
    }
}

@Composable
private fun ErrorText(error: UiText) {
    Text(
        text = error.asString(),
        style = MaterialTheme.typography.bodySmall.copy(
            color = SystemTheme.colors.accentError,
            fontWeight = FontWeight.SemiBold
        )
    )
}

private data class EquipmentOption(
    val type: EquipmentType,
    val title: String,
    val subtitle: String
)

private val EQUIPMENT_OPTIONS = listOf(
    EquipmentOption(EquipmentType.DUMBBELL, "Гантелі", "Домашні або регульовані гантелі"),
    EquipmentOption(EquipmentType.BARBELL, "Штанга", "База для силового прогресу"),
    EquipmentOption(EquipmentType.BENCH, "Лава", "Жими, тяги та стабільна опора"),
    EquipmentOption(EquipmentType.PULL_UP_BAR, "Турнік", "Підтягування та вертикальна тяга"),
    EquipmentOption(EquipmentType.BANDS, "Резинки", "Мобільність, активація, легкі тяги"),
    EquipmentOption(EquipmentType.MACHINE, "Зал і тренажери", "Тренажери та блочні станції")
)

@Composable
private fun OnboardingStep.accent(): Color {
    val colors = SystemTheme.colors
    return when (this) {
        OnboardingStep.NAME -> colors.accentPrimary
        OnboardingStep.GOAL -> colors.accentWarning
        OnboardingStep.EQUIPMENT -> colors.accentSuccess
        OnboardingStep.EXPERIENCE -> colors.accentAi
        OnboardingStep.CYCLE -> colors.accentPrimary
    }
}

private fun OnboardingStep.icon(): ImageVector =
    when (this) {
        OnboardingStep.NAME -> Icons.Filled.Person
        OnboardingStep.GOAL -> Icons.Filled.Flag
        OnboardingStep.EQUIPMENT -> Icons.Filled.FitnessCenter
        OnboardingStep.EXPERIENCE -> Icons.Filled.Whatshot
        OnboardingStep.CYCLE -> Icons.Filled.Settings
    }

private fun OnboardingStep.title(): String =
    when (this) {
        OnboardingStep.NAME -> "Як вас звати?"
        OnboardingStep.GOAL -> "Головна ціль"
        OnboardingStep.EQUIPMENT -> "Доступне обладнання"
        OnboardingStep.EXPERIENCE -> "Рівень досвіду"
        OnboardingStep.CYCLE -> "Розклад тренувань"
    }

@Composable
private fun OnboardingGoal.accent(): Color {
    val colors = SystemTheme.colors
    return when (this) {
        OnboardingGoal.BUILD_STRENGTH -> colors.accentWarning
        OnboardingGoal.BUILD_MUSCLE -> colors.accentPrimary
        OnboardingGoal.LOSE_WEIGHT -> colors.accentSuccess
        OnboardingGoal.BUILD_HABIT -> colors.accentAi
    }
}

private fun OnboardingGoal.title(): String =
    when (this) {
        OnboardingGoal.BUILD_STRENGTH -> "Сила"
        OnboardingGoal.BUILD_MUSCLE -> "М'язи"
        OnboardingGoal.LOSE_WEIGHT -> "Форма"
        OnboardingGoal.BUILD_HABIT -> "Стабільність"
    }

private fun OnboardingGoal.subtitle(): String =
    when (this) {
        OnboardingGoal.BUILD_STRENGTH -> "Рекомендований старт: 5 підходів по 5"
        OnboardingGoal.BUILD_MUSCLE -> "Рекомендований старт: 4 підходи по 10"
        OnboardingGoal.LOSE_WEIGHT -> "Рекомендований старт: 3 підходи по 15"
        OnboardingGoal.BUILD_HABIT -> "Рекомендований старт: 3 підходи по 8"
    }

private fun OnboardingExperience.title(): String =
    when (this) {
        OnboardingExperience.BEGINNER -> "Новачок"
        OnboardingExperience.RETURNING -> "Повертаюся"
        OnboardingExperience.INTERMEDIATE -> "Досвідчений"
    }

private fun OnboardingExperience.subtitle(): String =
    when (this) {
        OnboardingExperience.BEGINNER -> "Почати з 1 рівня"
        OnboardingExperience.RETURNING -> "Почати з 2 рівня"
        OnboardingExperience.INTERMEDIATE -> "Почати з 3 рівня"
    }

private fun OnboardingCyclePreset.title(): String =
    when (this) {
        OnboardingCyclePreset.THREE_DAY -> "3-денний цикл"
        OnboardingCyclePreset.FOUR_DAY -> "4-денний цикл"
        OnboardingCyclePreset.FIVE_DAY -> "5-денний цикл"
    }

private fun OnboardingCyclePreset.subtitle(): String =
    when (this) {
        OnboardingCyclePreset.THREE_DAY -> "Компактний старт: менше днів, більше відновлення"
        OnboardingCyclePreset.FOUR_DAY -> "Збалансований варіант: тренування чергуються з відпочинком"
        OnboardingCyclePreset.FIVE_DAY -> "Щільніший ритм для регулярної роботи"
    }
