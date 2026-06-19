package com.ihor.thesystem.feature.cycle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemCutCornerShape
import com.ihor.thesystem.core.ui.components.SystemHexIcon
import com.ihor.thesystem.core.ui.components.SystemMetricBlock
import com.ihor.thesystem.core.ui.components.SystemPanel
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemSectionTitle
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.feature.status.ui.WorkoutDialogHost
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.DayType
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusViewModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import kotlin.math.roundToInt

@Composable
fun CycleScreen(
    navController: NavHostController,
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
    statusViewModel: StatusViewModel = hiltViewModel()
) {
    val activeWorkout by workoutViewModel.activeWorkoutState.collectAsStateWithLifecycle()
    val cycleDays by workoutViewModel.cycleDaysState.collectAsStateWithLifecycle()
    val dialogState by workoutViewModel.dialogState.collectAsStateWithLifecycle()
    val settingsUiState by workoutViewModel.settingsUiState.collectAsStateWithLifecycle()
    val statusState by statusViewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors
    val statusData = (statusState as? UiState.Content<StatusUiData>)?.data
    val selectedCycleDayModel = cycleDays.firstOrNull { it.isSelected }
    val activeCycleDay = cycleDays.firstOrNull { it.isActive }?.dayNumber
    val selectedCycleDay = selectedCycleDayModel?.dayNumber
        ?: activeWorkout?.dayNumber
        ?: settingsUiState.selectedDay
    val openExercisePicker = {
        navController.navigate(
            Routes.ExercisePicker(
                source = Routes.PICKER_SOURCE_CYCLE,
                cycleDay = selectedCycleDay
            )
        )
    }

    RefreshOnResume(workoutViewModel::refreshForCurrentDay)
    RefreshOnResume(statusViewModel::refreshForCurrentDay)

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
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            SystemOverviewPanel(
                statusData = statusData,
                activeWorkout = activeWorkout,
                cycleDays = cycleDays
            )
            TrainingDaySwitcher(
                days = cycleDays,
                onSelectDay = workoutViewModel::onCycleDaySelected
            )
            statusData?.let {
                ParametersAndBonusesPanel(data = it)
            }
            if (activeCycleDay != null && selectedCycleDay != activeCycleDay) {
                TodayCycleActivationCard(
                    selectedDay = selectedCycleDay,
                    activeDay = activeCycleDay,
                    onActivateToday = workoutViewModel::onActivateSelectedCycleDayToday
                )
            }

            if (activeWorkout == null || activeWorkout?.exercises.isNullOrEmpty()) {
                EmptyCycleBlock(
                    selectedDay = cycleDays.firstOrNull { it.isSelected }?.dayNumber,
                    onEditDay = workoutViewModel::onOpenWorkoutSettings,
                    onAddExercise = openExercisePicker
                )
            } else {
                val workout = requireNotNull(activeWorkout)
                DayOverviewCard(
                    workout = workout,
                    onEditDay = workoutViewModel::onOpenWorkoutSettings
                )
                TrainingStackCard(
                    workout = workout,
                    onOpenExercise = { exercise ->
                        workoutViewModel.onOpenSetup(exercise.toMatrixEntry(workout))
                    }
                )
                CycleActions(
                    onStartWorkout = workoutViewModel::onOpenMainWorkout,
                    onEditDay = workoutViewModel::onOpenWorkoutSettings,
                    onAddExercise = openExercisePicker
                )
            }
            ArchitectCalloutPanel(
                onAnalyze = { navController.navigate(Routes.Architect) },
                onOpenFocus = workoutViewModel::onOpenWorkoutSettings
            )
        }

        WorkoutDialogHost(
            dialogState = dialogState,
            activeDayWorkout = activeWorkout,
            settingsUiState = settingsUiState,
            workoutViewModel = workoutViewModel,
            onOpenWorkoutAnalysis = { sessionId ->
                navController.navigate(Routes.WorkoutAnalysis(sessionId = sessionId))
            }
        )
    }
}

@Composable
private fun SystemOverviewPanel(
    statusData: StatusUiData?,
    activeWorkout: ActiveDayUiModel?,
    cycleDays: List<CycleDayUiModel>
) {
    val colors = SystemTheme.colors
    val progress = if (statusData != null && statusData.xpMax > 0) {
        statusData.xpTotal.toFloat() / statusData.xpMax.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)
    val activeDay = cycleDays.firstOrNull { it.isActive }?.dayNumber ?: activeWorkout?.dayNumber ?: 1
    val totalDays = cycleDays.size.coerceAtLeast(1)

    SystemPanel(modifier = Modifier.fillMaxWidth(), active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemCardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SystemSectionTitle(title = "Огляд системи")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = statusData?.level?.toString() ?: "-",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = colors.accentPrimary,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 1
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Рівень",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Ранг ${statusData?.globalRank?.name ?: "-"}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                    Text(
                        text = "Активний день: $activeDay / $totalDays",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SystemHexIcon(
                    icon = Icons.Filled.FitnessCenter,
                    accent = colors.accentPrimary,
                    modifier = Modifier.size(104.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SystemMetricBlock(
                    label = "Вправи",
                    value = activeWorkout?.exercises?.size?.toString() ?: "0",
                    modifier = Modifier.weight(1f)
                )
                SystemMetricBlock(
                    label = "Підходи",
                    value = activeWorkout?.totalSetCount()?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    accent = colors.accentSuccess
                )
                SystemMetricBlock(
                    label = "День",
                    value = activeDay.toString(),
                    modifier = Modifier.weight(1f),
                    accent = colors.accentAi
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${statusData?.xpTotal ?: 0} / ${statusData?.xpMax ?: 1000} XP",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            SystemProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
            )
        }
    }
}

@Composable
private fun TrainingDaySwitcher(
    days: List<CycleDayUiModel>,
    onSelectDay: (Int) -> Unit
) {
    SystemPanel(modifier = Modifier.fillMaxWidth(), contentPadding = SystemItemSpacing) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SystemSectionTitle(title = "План на тиждень")
            days.chunked(CYCLE_DAY_COLUMNS).forEach { rowDays ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowDays.forEach { day ->
                        CycleDayChip(
                            day = day,
                            onClick = { onSelectDay(day.dayNumber) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(CYCLE_DAY_COLUMNS - rowDays.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleDayChip(
    day: CycleDayUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    val subtitle = when {
        day.isActive -> "Сьогодні"
        day.workoutName != null -> day.workoutName
        day.type == DayType.WORKOUT -> "Тренування"
        else -> "Відновлення"
    }
    Column(
        modifier = modifier
            .height(108.dp)
            .clip(shape)
            .background(
                when {
                    day.isSelected -> colors.accentPrimary.copy(alpha = 0.18f)
                    day.isActive -> colors.accentPrimary.copy(alpha = 0.12f)
                    else -> colors.surfaceGlassSoft.copy(alpha = 0.74f)
                }
            )
            .border(
                if (day.isSelected || day.isActive) 1.4.dp else 1.dp,
                when {
                    day.isSelected || day.isActive -> colors.accentPrimary.copy(alpha = 0.72f)
                    else -> colors.borderSubtle
                },
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = when (day.dayNumber) {
                1 -> "ПН"
                2 -> "СР"
                3 -> "ПТ"
                4 -> "НД"
                else -> "Д${day.dayNumber}"
            },
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (day.isSelected || day.isActive) colors.accentPrimary else colors.textSecondary,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (day.type == DayType.WORKOUT) {
                day.dayNumber.toWorkoutLetter()
            } else {
                "R"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                color = if (day.isSelected || day.isActive) colors.textPrimary else colors.textSecondary,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1
        )
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun Int.toWorkoutLetter(): String =
    when (this) {
        1 -> "A"
        2 -> "B"
        3 -> "C"
        else -> toString()
    }

@Composable
private fun TodayCycleActivationCard(
    selectedDay: Int,
    activeDay: Int,
    onActivateToday: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Зробити День $selectedDay поточним?",
                subtitle = "Зараз сьогодні позначено як День $activeDay"
            )
            SystemButton(
                text = "Зробити сьогодні Днем $selectedDay",
                icon = Icons.Filled.Today,
                onClick = onActivateToday,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DayOverviewCard(
    workout: ActiveDayUiModel,
    onEditDay: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = "День ${workout.dayNumber} · ${workout.workoutName ?: "Тренування"}",
                subtitle = "Основна картка дня",
                trailing = {
                    CompactActionButton(
                        icon = Icons.Filled.Edit,
                        onClick = onEditDay
                    )
                }
            )
            workout.adjustmentReason?.let { reason ->
                AdjustmentReasonRow(reason = reason)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric(
                    label = "Вправи",
                    value = workout.exercises.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    label = "Підходи",
                    value = workout.totalSetCount().toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AdjustmentReasonRow(reason: String) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = colors.accentAi,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = reason,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textMuted,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            )
        )
    }
}

@Composable
private fun TrainingStackCard(
    workout: ActiveDayUiModel,
    onOpenExercise: (ExerciseWorkoutUiModel) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Тренувальний стек",
                subtitle = "Цільові ваги, підходи та повторення"
            )
            workout.exercises.forEachIndexed { index, exercise ->
                ExerciseStackRow(
                    index = index + 1,
                    exercise = exercise,
                    onClick = { onOpenExercise(exercise) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseStackRow(
    index: Int,
    exercise: ExerciseWorkoutUiModel,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.small))
                .background(colors.accentPrimarySoft)
                .border(
                    1.dp,
                    colors.accentPrimary.copy(alpha = 0.28f),
                    RoundedCornerShape(SystemTheme.shapes.small)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = exercise.nameUk ?: exercise.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ціль: ${exercise.targetWeightText()}",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = exercise.setSchemeText(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CycleActions(
    onStartWorkout: () -> Unit,
    onEditDay: () -> Unit,
    onAddExercise: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SystemButton(
                text = "Почати тренування",
                icon = Icons.Filled.PlayArrow,
                onClick = onStartWorkout,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Редагувати день",
                    icon = Icons.Filled.Edit,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Додати вправу",
                    icon = Icons.Filled.Add,
                    onClick = onAddExercise,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyCycleBlock(
    selectedDay: Int?,
    onEditDay: () -> Unit,
    onAddExercise: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = selectedDay?.let { "День $it без вправ" } ?: "Тренування не заплановано",
                subtitle = "Додайте вправу або змініть профіль обладнання"
            )
            Text(
                text = "Наступна дія: додати вправу до цього дня.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
            )
            SystemButton(
                text = "Додати вправу",
                icon = Icons.Filled.Add,
                onClick = onAddExercise,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Редагувати день",
                    icon = Icons.Filled.Settings,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Обладнання",
                    icon = Icons.Filled.FitnessCenter,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ParametersAndBonusesPanel(data: StatusUiData) {
    val colors = SystemTheme.colors
    val attributes = data.characterAttributes.entries.take(5)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        SystemPanel(
            modifier = Modifier.weight(1f),
            accent = colors.accentPrimary,
            contentPadding = 12.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemSectionTitle(title = "Параметри")
                if (attributes.isEmpty()) {
                    AttributeLine(
                        label = "Сила",
                        value = data.level.toFloat(),
                        accent = colors.accentError
                    )
                    AttributeLine(
                        label = "Дисципліна",
                        value = data.currentStreak.toFloat() * 10f,
                        accent = colors.accentSuccess
                    )
                } else {
                    attributes.forEachIndexed { index, entry ->
                        AttributeLine(
                            label = entry.key.displayName(),
                            value = entry.value,
                            accent = when (index % 5) {
                                0 -> colors.accentError
                                1 -> colors.accentPrimary
                                2 -> colors.accentSuccess
                                3 -> colors.accentAi
                                else -> colors.accentWarning
                            }
                        )
                    }
                }
            }
        }
        SystemPanel(
            modifier = Modifier.weight(1f),
            accent = colors.accentSuccess,
            contentPadding = 12.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemSectionTitle(title = "Активні бонуси")
                BonusLine(text = "Серія ${data.currentStreak} днів", value = "+${(data.currentStreak * 2).coerceAtMost(20)}% XP")
                BonusLine(text = "Планувальник", value = "${data.todos.count { it.isCompleted }}/${data.todos.size.coerceAtLeast(1)}")
                BonusLine(text = "Рівень ${data.level}", value = "R${data.globalRank.name}")
            }
        }
    }
}

@Composable
private fun AttributeLine(
    label: String,
    value: Float,
    accent: androidx.compose.ui.graphics.Color
) {
    val colors = SystemTheme.colors
    val normalized = (value / 100f).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value.roundToInt().toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        }
        SystemProgressBar(
            progress = normalized,
            accent = accent,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
    }
}

@Composable
private fun BonusLine(
    text: String,
    value: String
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SystemCutCornerShape(8.dp))
            .background(colors.accentSuccess.copy(alpha = 0.08f))
            .border(1.dp, colors.accentSuccess.copy(alpha = 0.18f), SystemCutCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = colors.accentSuccess,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.accentSuccess,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1
        )
    }
}

private fun com.ihor.thesystem.domain.model.MuscleGroup.displayName(): String =
    when (this) {
        com.ihor.thesystem.domain.model.MuscleGroup.CHEST -> "Груди"
        com.ihor.thesystem.domain.model.MuscleGroup.BACK -> "Спина"
        com.ihor.thesystem.domain.model.MuscleGroup.SHOULDERS -> "Плечі"
        com.ihor.thesystem.domain.model.MuscleGroup.QUADS -> "Квадрицепс"
        com.ihor.thesystem.domain.model.MuscleGroup.HAMSTRINGS_GLUTES -> "Ноги"
        com.ihor.thesystem.domain.model.MuscleGroup.ARMS -> "Руки"
        com.ihor.thesystem.domain.model.MuscleGroup.ABS -> "Прес"
        com.ihor.thesystem.domain.model.MuscleGroup.LEGS -> "Ноги"
        com.ihor.thesystem.domain.model.MuscleGroup.CORE -> "Кор"
    }

@Composable
private fun ArchitectCalloutPanel(
    onAnalyze: () -> Unit,
    onOpenFocus: () -> Unit
) {
    val colors = SystemTheme.colors
    SystemPanel(
        modifier = Modifier.fillMaxWidth(),
        accent = colors.accentAi
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemCardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SystemHexIcon(
                    icon = Icons.Filled.AutoAwesome,
                    accent = colors.accentAi,
                    modifier = Modifier.size(72.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SystemSectionTitle(
                        title = "AI-Архітектор",
                        subtitle = "Система може проаналізувати прогрес і запропонувати наступний крок."
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Проаналізувати",
                    icon = Icons.Filled.AutoAwesome,
                    onClick = onAnalyze,
                    accent = colors.accentAi,
                    glow = true,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Налаштувати фокус",
                    icon = Icons.Filled.Settings,
                    onClick = onOpenFocus,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accentPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun ActiveDayUiModel.totalSetCount(): Int =
    exercises.sumOf { exercise -> exercise.recommendedSets ?: exercise.sets.size }

private fun ExerciseWorkoutUiModel.targetWeightText(): String =
    recommendedWeight
        ?.takeIf { it > 0.0 }
        ?.let { "${it.formatWeight()} кг" }
        ?: "не задано"

private fun ExerciseWorkoutUiModel.setSchemeText(): String {
    val sets = recommendedSets ?: this.sets.size.takeIf { it > 0 } ?: 1
    val reps = recommendedReps ?: 0
    return if (reps > 0) "$sets x $reps" else "$sets підх."
}

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

private fun ExerciseWorkoutUiModel.toMatrixEntry(workout: ActiveDayUiModel): MatrixEntryUiModel =
    workout.matrixEntries.find { it.exerciseId == exerciseId }
        ?: MatrixEntryUiModel(
            exerciseId = exerciseId,
            exerciseName = nameUk ?: name,
            startWeight = 0f,
            targetWeight = recommendedWeight?.toFloat() ?: 0f,
            currentWeight = recommendedWeight?.toFloat() ?: 0f,
            targetWeightNote = null,
            weeklyStep = 0f,
            progressPercent = 0f,
            currentRank = Rank.E,
            usesExternalLoad = trackingMode.usesWeightInput
        )

private const val CYCLE_DAY_COLUMNS = 4
