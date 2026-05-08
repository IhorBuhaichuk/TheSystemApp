package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentPrimarySoft
import com.ihor.thesystem.core.theme.AccentSuccess
import com.ihor.thesystem.core.theme.AccentWarning
import com.ihor.thesystem.core.theme.BorderActive
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.MixedDayGradientEnd
import com.ihor.thesystem.core.theme.MixedDayGradientStart
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.theme.TrainingDayColor
import com.ihor.thesystem.core.theme.WorkDayColor
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemMetricCard
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarDayCompletionStatus
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarDayUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarUiState
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class CalendarDayVisualType {
    Work,
    Training,
    Mixed,
    Rest
}

@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDay = remember(uiState.days, uiState.selectedDate) {
        uiState.selectedDate?.let { selectedDate ->
            uiState.days.firstOrNull { it.date == selectedDate }
        } ?: uiState.days.firstOrNull { it.isToday }
    }

    RefreshOnResume(viewModel::refreshVisibleData)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            CalendarHeader(
                currentMonth = uiState.currentMonth,
                onBack = { navController.popBackStack() },
                onPreviousMonth = { viewModel.onMonthChange(uiState.currentMonth.minusMonths(1)) },
                onNextMonth = { viewModel.onMonthChange(uiState.currentMonth.plusMonths(1)) },
                onOpenSettings = { navController.navigate(Routes.CalendarSettings) }
            )

            if (uiState.isLoading && uiState.days.isEmpty()) {
                CalendarLoadingBlock()
            } else {
                MonthOverviewPanel(
                    uiState = uiState,
                    onDateSelected = viewModel::onDateSelected
                )
                SelectedDayDetailsPanel(
                    day = selectedDay,
                    onOpenDay = {
                        selectedDay?.let { day ->
                            viewModel.onDateSelected(day.date)
                            if (day.hasTrainingPlan) {
                                navController.navigate(Routes.Cycle)
                            } else {
                                navController.navigate(Routes.Status)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBack
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Календар",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = currentMonth.toLocalizedMonthYear(),
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onPreviousMonth
        )
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = onNextMonth
        )
        GlassIconButton(
            icon = Icons.Filled.Settings,
            onClick = onOpenSettings,
            active = true
        )
    }
}

@Composable
private fun CalendarLoadingBlock() {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
    }
}

@Composable
private fun MonthOverviewPanel(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = uiState.currentMonth.toLocalizedMonthYear(),
                subtitle = "Заплановано, виконано, пропущено"
            )
            CalendarGrid(
                days = uiState.days,
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected
            )
            CalendarLegend()
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDayUiModel>,
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val dayModels = remember(days) { days.associateBy { it.date } }
    val startOffset = currentMonth.atDay(1).dayOfWeek.value - 1
    val gridStart = currentMonth.atDay(1).minusDays(startOffset.toLong())

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        repeat(6) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(7) { column ->
                    val date = gridStart.plusDays((row * 7 + column).toLong())
                    val model = dayModels[date]
                    CalendarDayCell(
                        date = date,
                        model = model,
                        isInCurrentMonth = date.month == currentMonth.month,
                        isSelected = selectedDate == date,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    model: CalendarDayUiModel?,
    isInCurrentMonth: Boolean,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = isInCurrentMonth && model != null
    val shape = RoundedCornerShape(12.dp)
    val isToday = model?.isToday == true

    Box(
        modifier = modifier
            .aspectRatio(0.86f)
            .clip(shape)
            .background(dayBackground(model))
            .border(
                width = if (isSelected || isToday) 1.4.dp else 1.dp,
                color = dayBorderColor(model, isSelected, isToday),
                shape = shape
            )
            .clickable(enabled = enabled) { onDateSelected(date) }
            .padding(6.dp)
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = when {
                    !isInCurrentMonth -> TextMuted.copy(alpha = 0.32f)
                    isSelected || isToday -> AccentPrimary
                    else -> TextPrimary
                },
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.align(Alignment.TopStart)
        )

        model?.let { day ->
            DayStatusIndicator(
                status = day.completionStatus,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun DayStatusIndicator(
    status: CalendarDayCompletionStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        CalendarDayCompletionStatus.COMPLETED -> Box(
            modifier = modifier
                .size(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentSuccess.copy(alpha = 0.12f))
                .border(1.dp, AccentSuccess.copy(alpha = 0.36f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = AccentSuccess,
                modifier = Modifier.size(10.dp)
            )
        }
        CalendarDayCompletionStatus.PARTIAL -> StatusDot(
            color = AccentWarning,
            modifier = modifier
        )
        CalendarDayCompletionStatus.MISSED -> Canvas(modifier = modifier.size(16.dp)) {
            drawCircle(
                color = AccentWarning.copy(alpha = 0.42f),
                radius = size.minDimension / 2.8f,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
        CalendarDayCompletionStatus.PLANNED -> StatusDot(
            color = TextMuted.copy(alpha = 0.52f),
            modifier = modifier
        )
        CalendarDayCompletionStatus.NO_DATA -> Spacer(modifier = modifier.size(16.dp))
    }
}

@Composable
private fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        drawCircle(color = color.copy(alpha = 0.9f), radius = 2.8.dp.toPx())
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = WorkDayColor, text = "Робота")
        LegendItem(color = TrainingDayColor, text = "Тренування")
        LegendItem(color = AccentSuccess, text = "Виконано")
        LegendItem(color = AccentWarning, text = "Увага")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.74f))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SelectedDayDetailsPanel(
    day: CalendarDayUiModel?,
    onOpenDay: () -> Unit
) {
    DarkGlassCard(
        modifier = Modifier.fillMaxWidth(),
        active = day?.isToday == true
    ) {
        if (day == null) {
            Text(
                text = "Оберіть день у поточному місяці",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            return@DarkGlassCard
        }

        val hasAnyPlan = day.hasTrainingPlan || day.totalTasks > 0
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = day.date.toLocalizedDate(),
                subtitle = "День ${day.cycleDay} тренувального циклу",
                trailing = {
                    SystemStatusChip(
                        text = day.completionStatus.toDisplayText(),
                        active = day.completionStatus == CalendarDayCompletionStatus.COMPLETED
                    )
                }
            )

            if (!day.isCalendarCycleConfigured) {
                Text(
                    text = "Налаштуйте календарний цикл, щоб бачити тут свої результати.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                return@Column
            }

            if (!hasAnyPlan) {
                Text(
                    text = "На цей день нічого не заплановано",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemMetricCard(
                    label = "Тип",
                    value = day.label,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricCard(
                    label = "To-do",
                    value = "${day.completedTasks}/${day.totalTasks}",
                    modifier = Modifier.weight(1f)
                )
            }

            DarkGlassCard(contentPadding = 12.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = day.plannedWorkoutName ?: "Тренування не заплановано",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (day.hasTrainingPlan) {
                                "${day.plannedExerciseCount} вправ у плані"
                            } else {
                                "Календарний день без тренувального блоку"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (day.hasCompletedWorkout) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = AccentSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            SystemButton(
                text = "Відкрити день",
                icon = if (day.hasTrainingPlan) Icons.Filled.FitnessCenter else Icons.Filled.Today,
                onClick = onOpenDay,
                glow = day.hasTrainingPlan,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    active: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(shape)
            .background(if (active) AccentPrimarySoft else Color.White.copy(alpha = 0.035f))
            .border(1.dp, if (active) BorderActive else BorderSubtle, shape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) AccentPrimary else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun dayBackground(model: CalendarDayUiModel?): Brush {
    if (model == null) {
        return Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.012f), Color.White.copy(alpha = 0.006f))
        )
    }

    return when (model.visualType()) {
        CalendarDayVisualType.Work -> Brush.verticalGradient(
            listOf(WorkDayColor.copy(alpha = 0.2f), Color.White.copy(alpha = 0.022f))
        )
        CalendarDayVisualType.Training -> Brush.verticalGradient(
            listOf(TrainingDayColor.copy(alpha = 0.18f), Color.White.copy(alpha = 0.022f))
        )
        CalendarDayVisualType.Mixed -> Brush.linearGradient(
            colors = listOf(
                MixedDayGradientStart.copy(alpha = 0.26f),
                MixedDayGradientEnd.copy(alpha = 0.24f)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
        CalendarDayVisualType.Rest -> Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.026f), Color.White.copy(alpha = 0.012f))
        )
    }
}

private fun dayBorderColor(
    model: CalendarDayUiModel?,
    isSelected: Boolean,
    isToday: Boolean
): Color =
    when {
        isSelected -> BorderActive
        isToday -> AccentPrimary.copy(alpha = 0.56f)
        model?.completionStatus == CalendarDayCompletionStatus.MISSED -> AccentWarning.copy(alpha = 0.42f)
        else -> BorderSubtle
    }

private fun CalendarDayUiModel.visualType(): CalendarDayVisualType {
    val isWork = calendarDayType.isWorkLike()
    return when {
        isWork && hasTrainingPlan -> CalendarDayVisualType.Mixed
        hasTrainingPlan -> CalendarDayVisualType.Training
        isWork -> CalendarDayVisualType.Work
        else -> CalendarDayVisualType.Rest
    }
}

private fun CalendarCycleDayType.isWorkLike(): Boolean =
    this == CalendarCycleDayType.WORK || this == CalendarCycleDayType.NIGHT

private fun CalendarDayCompletionStatus.toDisplayText(): String =
    when (this) {
        CalendarDayCompletionStatus.COMPLETED -> "Виконано"
        CalendarDayCompletionStatus.PARTIAL -> "Частково"
        CalendarDayCompletionStatus.MISSED -> "Пропущено"
        CalendarDayCompletionStatus.PLANNED -> "Заплановано"
        CalendarDayCompletionStatus.NO_DATA -> "Немає даних"
    }

private fun YearMonth.toLocalizedMonthYear(): String {
    val locale = Locale.getDefault()
    val month = month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    return "$month $year"
}

private fun LocalDate.toLocalizedDate(): String {
    val locale = Locale.getDefault()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
    return format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
