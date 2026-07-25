package com.ihor.thesystem.feature.architect.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.AnnualProgressionAdjustment
import com.ihor.thesystem.domain.model.AnnualProgressionExercisePlan
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyTarget
import com.ihor.thesystem.domain.model.AnnualProgressionPlan
import com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionExerciseUiModel
import com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionPlanUiState
import com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionPlanViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AnnualProgressionPlanScreen(
    onBack: () -> Unit,
    onOpenExercisePicker: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnnualProgressionPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = SystemTheme.colors

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        Toast.makeText(context, message.asString(context), Toast.LENGTH_SHORT).show()
        viewModel.onMessageShown()
    }

    Box(
        modifier = modifier
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
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            AnnualPlanHeader(onBack = onBack)
            AdaptationStatusBlock(
                state = uiState,
                onUseToday = viewModel::onUseTodayAsStartDate,
                onSelectDate = viewModel::onStartDateSelected
            )
            SelectedExercisesBlock(
                state = uiState,
                onOpenExercisePicker = onOpenExercisePicker,
                onRemoveExercise = viewModel::onRemoveExercise,
                onToggleExpanded = viewModel::onExerciseExpanded,
                onTargetWeightChanged = viewModel::onTargetWeightChanged,
                onInventoryStepChanged = viewModel::onInventoryStepChanged
            )
            GenerationBlock(
                state = uiState,
                onGeneratePlan = viewModel::onGeneratePlan
            )
            uiState.generatedPlan?.let { plan ->
                AnnualPlanResultBlock(
                    plan = plan,
                    expandedResultIds = uiState.expandedResultIds,
                    isSaving = uiState.isSaving,
                    onToggleExpanded = viewModel::onResultExpanded,
                    onSavePlan = viewModel::onSavePlan
                )
            }
        }
    }
}

@Composable
private fun AnnualPlanHeader(onBack: () -> Unit) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.overlayLight)
                .border(1.dp, colors.borderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = colors.textSecondary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                    text = "Річний план прогресу",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                    text = "12 місяців для вибраних вправ",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdaptationStatusBlock(
    state: AnnualProgressionPlanUiState,
    onUseToday: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val colors = SystemTheme.colors
    var showDatePicker by remember { mutableStateOf(false) }

    DarkGlassCard(active = state.isAdaptationComplete) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Збір стартових даних",
                subtitle = "Потрібно 2 тижні тренувань"
            )
            Text(
                text = "Перші 2 тижні система збирає стартові дані. Після цього можна сформувати річний графік з цілями на кожен місяць.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Сьогодні",
                    onClick = onUseToday,
                    accent = colors.accentAi,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Вибрати дату",
                    onClick = { showDatePicker = true },
                    accent = colors.accentAi,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SystemStatusChip(
                    text = "Старт: ${state.startDate.formatDate()}",
                    accent = colors.accentAi,
                    active = true
                )
                SystemStatusChip(
                    text = "Після: ${state.adaptationEndsOn.formatDate()}",
                    accent = colors.accentPrimary,
                    active = state.isAdaptationComplete
                )
            }
            Text(
                text = if (state.isAdaptationComplete) {
                    "Адаптацію завершено. Можна формувати фінальний річний план."
                } else {
                    "До завершення збору даних: ${state.adaptationRemainingDays} дн. Після цього можна буде створити точніший план."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (state.isAdaptationComplete) colors.accentAi else colors.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

    if (showDatePicker) {
        AnnualStartDatePickerDialog(
            initialDate = state.startDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                onSelectDate(date)
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnualStartDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val colors = SystemTheme.colors
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toLocalDate()
                        ?.let(onConfirm)
                }
            ) {
                Text(text = "Обрати", color = colors.accentAi)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Скасувати", color = colors.textSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun SelectedExercisesBlock(
    state: AnnualProgressionPlanUiState,
    onOpenExercisePicker: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onToggleExpanded: (Int) -> Unit,
    onTargetWeightChanged: (Int, String) -> Unit,
    onInventoryStepChanged: (Int, String) -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Вибрані вправи",
                subtitle = if (state.selectedExercises.isEmpty()) {
                    "Додай вправи для прогнозу"
                } else {
                    "${state.selectedExercises.size} вправ у плані"
                },
                trailing = {
                    SystemButton(
                        text = "Додати",
                        icon = Icons.Filled.Add,
                        onClick = onOpenExercisePicker,
                        accent = colors.accentAi
                    )
                }
            )
            if (state.selectedExercises.isEmpty()) {
                EmptyAnnualBlock(text = "Список порожній. Обери вправи з бази, щоб підготувати річний прогноз.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.selectedExercises.forEach { exercise ->
                        SelectedExerciseCard(
                            exercise = exercise,
                            onRemove = { onRemoveExercise(exercise.exerciseId) },
                            onToggleExpanded = { onToggleExpanded(exercise.exerciseId) },
                            onTargetWeightChanged = { value ->
                                onTargetWeightChanged(exercise.exerciseId, value)
                            },
                            onInventoryStepChanged = { value ->
                                onInventoryStepChanged(exercise.exerciseId, value)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedExerciseCard(
    exercise: AnnualProgressionExerciseUiModel,
    onRemove: () -> Unit,
    onToggleExpanded: () -> Unit,
    onTargetWeightChanged: (String) -> Unit,
    onInventoryStepChanged: (String) -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemItemSpacing),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Поточна: ${exercise.currentWorkingWeight.formatNullableWeight()} · Повт.: ${exercise.reps ?: "—"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleExpanded, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = if (exercise.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = colors.accentAi
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Видалити",
                    tint = colors.textMuted
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnnualNumberField(
                value = exercise.targetWeightInput,
                onValueChange = onTargetWeightChanged,
                    label = "Ціль через 12 місяців",
                modifier = Modifier.weight(1f)
            )
            AnnualNumberField(
                value = exercise.inventoryStepInput,
                onValueChange = onInventoryStepChanged,
                    label = "Мінімальний крок ваги",
                modifier = Modifier.weight(1f)
            )
        }

        if (exercise.isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AnnualMetricLine(label = "Останнє тренування", value = exercise.lastTrainingTimestamp.formatTimestamp())
                AnnualMetricLine(label = "Розрахунковий максимум", value = exercise.estimatedOneRepMax.formatNullableWeight())
                if (!exercise.canGenerate) {
                    Text(
                    text = "Вкажіть поточну вагу, вищу ціль і найменший доступний крок ваги.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.textMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnualNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label, color = colors.textMuted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = systemOutlinedTextFieldColors(accent = colors.accentAi),
        shape = RoundedCornerShape(SystemTheme.shapes.medium)
    )
}

@Composable
private fun AnnualMetricLine(label: String, value: String) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GenerationBlock(
    state: AnnualProgressionPlanUiState,
    onGeneratePlan: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Створення плану",
                subtitle = "12 місяців · ціль на кожен місяць"
            )
            Text(
                text = when {
                    !state.isAdaptationComplete -> "План буде доступний після перших 2 тижнів збору даних."
                    state.selectedExercises.isEmpty() -> "Додай хоча б одну вправу."
                    !state.canGenerate -> "Заповни показники для кожної вправи."
                    else -> "Дані готові до формування плану."
                },
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
            )
            SystemButton(
                text = if (state.isGenerating) "Створюю…" else "Створити план",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onGeneratePlan,
                accent = colors.accentAi,
                enabled = !state.isGenerating && state.canGenerate,
                glow = state.canGenerate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnnualPlanResultBlock(
    plan: AnnualProgressionPlan,
    expandedResultIds: Set<Int>,
    isSaving: Boolean,
    onToggleExpanded: (Int) -> Unit,
    onSavePlan: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Результат",
                subtitle = "План стартує ${plan.startDate.formatDate()}"
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plan.exercises.forEach { exercisePlan ->
                    AnnualExercisePlanCard(
                        plan = exercisePlan,
                        expanded = exercisePlan.exerciseId in expandedResultIds,
                        onToggleExpanded = { onToggleExpanded(exercisePlan.exerciseId) }
                    )
                }
            }
            SystemButton(
                text = if (isSaving) "Зберігаю..." else "Зберегти план",
                icon = Icons.Filled.Save,
                onClick = onSavePlan,
                accent = colors.accentAi,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnnualExercisePlanCard(
    plan: AnnualProgressionExercisePlan,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.accentAiSoft.copy(alpha = 0.34f))
            .border(1.dp, colors.accentAi.copy(alpha = 0.18f), shape)
            .padding(SystemItemSpacing),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = plan.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${plan.currentWeight.formatWeight()} кг → ${plan.targetWeight.formatWeight()} кг",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.accentAi,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            SystemStatusChip(text = "За планом", accent = colors.accentAi, active = true)
            IconButton(onClick = onToggleExpanded, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = colors.accentAi
                )
            }
        }
        if (expanded) {
            AnnualMetricLine(label = "Ідеальний крок", value = "${plan.idealMonthlyStep.formatWeight()} кг/міс")
            AnnualMetricLine(label = "Крок інвентарю", value = "${plan.inventoryStep.formatWeight()} кг")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(plan.monthlyTargets, key = { it.monthIndex }) { target ->
                    MonthlyTargetChip(target = target)
                }
            }
        }
    }
}

@Composable
private fun MonthlyTargetChip(target: AnnualProgressionMonthlyTarget) {
    val colors = SystemTheme.colors
    val accent = when (target.adjustment) {
        AnnualProgressionAdjustment.Baseline -> colors.textMuted
        AnnualProgressionAdjustment.StandardStep -> colors.accentAi
        AnnualProgressionAdjustment.Plateau -> colors.accentPrimary
        AnnualProgressionAdjustment.ForcedJump -> colors.accentAi
    }
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(SystemTheme.shapes.small))
            .background(colors.surfaceGlassSoft)
            .border(1.dp, accent.copy(alpha = 0.24f), RoundedCornerShape(SystemTheme.shapes.small))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
                    text = "Міс. ${target.monthIndex}",
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textMuted,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = target.weight.formatWeight(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            )
        )
        if (target.adjustment == AnnualProgressionAdjustment.Plateau) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EmptyAnnualBlock(text: String) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemCardPadding),
        style = MaterialTheme.typography.bodySmall.copy(
            color = colors.textMuted,
            fontWeight = FontWeight.Medium
        )
    )
}

private fun LocalDate.formatDate(): String =
    format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun Long?.formatTimestamp(): String =
    this?.toLocalDate()?.formatDate() ?: "—"

private fun Double?.formatNullableWeight(): String =
    this?.let { "${it.formatWeight()} кг" } ?: "—"

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
