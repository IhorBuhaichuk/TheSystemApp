package com.ihor.thesystem.feature.cycle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
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
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemCutCornerShape
import com.ihor.thesystem.core.ui.components.SystemHexIcon
import com.ihor.thesystem.core.ui.components.SystemPanel
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemSectionTitle
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemControlShape
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.core.ui.components.systemPlateShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.feature.status.ui.WorkoutDialogHost
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.DayType
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusViewModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
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
                .testTag(SystemUiTestTags.CYCLE_SCROLL)
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            SystemOverviewPanel(
                statusData = statusData,
                activeWorkout = activeWorkout,
                cycleDays = cycleDays
            )
            ActiveCyclePanel(
                statusData = statusData,
                activeWorkout = activeWorkout,
                cycleDays = cycleDays,
                onEditCycle = workoutViewModel::onOpenWorkoutSettings
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
    val workoutDays = cycleDays.count { it.type == DayType.WORKOUT }
    val phaseLabel = if ((activeWorkout?.exercises?.isNotEmpty() == true) || workoutDays > 0) {
        "Build"
    } else {
        "Recovery"
    }

    SystemPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(SystemUiTestTags.CYCLE_OVERVIEW),
        active = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemCardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SystemSectionTitle(title = "Огляд системи")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        SystemHeroStat(
                            label = "Рівень",
                            value = statusData?.level?.toString() ?: "-",
                            accent = colors.accentPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        SystemHeroStat(
                            label = "Ранг",
                            value = statusData?.globalRank?.name ?: "-",
                            accent = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "Активна фаза: $phaseLabel     День циклу: $activeDay / $totalDays",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SystemHexIcon(
                    icon = Icons.Filled.FitnessCenter,
                    accent = colors.accentPrimary,
                    modifier = Modifier.size(96.dp)
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
private fun SystemHeroStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label.toSystemSentenceCase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge.copy(
                color = accent,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActiveCyclePanel(
    statusData: StatusUiData?,
    activeWorkout: ActiveDayUiModel?,
    cycleDays: List<CycleDayUiModel>,
    onEditCycle: () -> Unit
) {
    val colors = SystemTheme.colors
    val selectedDay = cycleDays.firstOrNull { it.isSelected } ?: cycleDays.firstOrNull { it.isActive }
    val activeDay = cycleDays.firstOrNull { it.isActive }?.dayNumber ?: activeWorkout?.dayNumber ?: selectedDay?.dayNumber ?: 1
    val totalDays = cycleDays.size.coerceAtLeast(1)
    val workoutDays = cycleDays.count { it.type == DayType.WORKOUT }
    val cycleName = activeWorkout?.workoutName
        ?: selectedDay?.workoutName
        ?: if (workoutDays > 1) "Full Body / Workout A-B" else "Full Body"
    val nextWorkout = activeWorkout?.workoutName
        ?: cycleDays.firstOrNull { it.type == DayType.WORKOUT && it.isActive }?.workoutName
        ?: selectedDay?.workoutName
        ?: "Workout ${activeDay.toWorkoutLetter()}"
    val progressLabel = statusData?.let { data ->
        "${data.monthWorkoutsCompleted} / ${data.monthWorkoutsTotal.coerceAtLeast(1)}"
    } ?: "$workoutDays / $totalDays"

    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 320.dp
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ActiveCycleDetails(
                        cycleName = cycleName,
                        activeDay = activeDay,
                        totalDays = totalDays,
                        workoutDays = workoutDays,
                        nextWorkout = nextWorkout,
                        compact = true
                    )
                    ActiveCycleActions(
                        progressLabel = progressLabel,
                        onEditCycle = onEditCycle,
                        compact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SystemCardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActiveCycleDetails(
                        cycleName = cycleName,
                        activeDay = activeDay,
                        totalDays = totalDays,
                        workoutDays = workoutDays,
                        nextWorkout = nextWorkout,
                        compact = false,
                        modifier = Modifier.weight(1f)
                    )
                    ActiveCycleActions(
                        progressLabel = progressLabel,
                        onEditCycle = onEditCycle,
                        compact = false,
                        modifier = Modifier.weight(0.68f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveCycleDetails(
    cycleName: String,
    activeDay: Int,
    totalDays: Int,
    workoutDays: Int,
    nextWorkout: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        SystemSectionTitle(title = "Активний цикл")
        CycleDetailLine(
            icon = Icons.Filled.Settings,
            label = "Цикл",
            value = cycleName,
            compact = compact
        )
        CycleDetailLine(
            icon = Icons.Filled.Today,
            label = "День циклу",
            value = "$activeDay / $totalDays",
            accent = colors.accentAi,
            compact = compact
        )
        CycleDetailLine(
            icon = Icons.Filled.FitnessCenter,
            label = "Тренувань на тиждень",
            value = workoutDays.toString(),
            accent = colors.accentPrimary,
            compact = compact
        )
        CycleDetailLine(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            label = "Наступне тренування",
            value = nextWorkout,
            compact = compact
        )
    }
}

@Composable
private fun ActiveCycleActions(
    progressLabel: String,
    onEditCycle: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    if (compact) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SystemHexIcon(
                icon = Icons.Filled.Edit,
                accent = colors.accentPrimary,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "Прогрес $progressLabel",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textMuted,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.65f)
            )
            SystemButton(
                text = "Редагувати цикл",
                icon = Icons.Filled.Edit,
                onClick = onEditCycle,
                glow = true,
                modifier = Modifier.weight(1.35f)
            )
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SystemHexIcon(
                icon = Icons.Filled.Edit,
                accent = colors.accentPrimary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Прогрес $progressLabel",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textMuted,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SystemButton(
                text = "Редагувати цикл",
                icon = Icons.Filled.Edit,
                onClick = onEditCycle,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CycleDetailLine(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color? = null,
    compact: Boolean = false
) {
    val colors = SystemTheme.colors
    val iconTint = accent ?: colors.textSecondary
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )
        if (compact) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textMuted,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
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
    val shape = systemPlateShape()
    val accent = when {
        day.isSelected || day.isActive -> colors.accentPrimary
        day.type == DayType.WORKOUT -> colors.textSecondary
        else -> colors.textMuted
    }
    val subtitle = when {
        day.isActive -> "Сьогодні"
        day.workoutName != null -> day.workoutName
        day.type == DayType.WORKOUT -> "Тренування"
        else -> "Відновлення"
    }
    Column(
        modifier = modifier
            .heightIn(min = 108.dp)
            .techSurface(
                shape = shape,
                active = day.isSelected || day.isActive,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .systemClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = when (day.dayNumber) {
                1 -> "Пн"
                2 -> "Ср"
                3 -> "Пт"
                4 -> "Нд"
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
            text = subtitle.toSystemSentenceCase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textMuted,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
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
                        accent = colors.statusProgress
                    )
                    AttributeLine(
                        label = "Дисципліна",
                        value = data.currentStreak.toFloat() * 10f,
                        accent = colors.statusProgress
                    )
                } else {
                    attributes.forEach { entry ->
                        AttributeLine(
                            label = entry.key.displayName(),
                            value = entry.value,
                            accent = colors.statusProgress
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
                text = label.toSystemSentenceCase(),
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
    val shape = SystemCutCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentSuccess,
                role = TechSurfaceRole.Plate
            )
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
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = systemControlShape()
    Box(
        modifier = Modifier
            .size(48.dp)
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Plate
            )
            .systemClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.accentPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private const val CYCLE_DAY_COLUMNS = 4
