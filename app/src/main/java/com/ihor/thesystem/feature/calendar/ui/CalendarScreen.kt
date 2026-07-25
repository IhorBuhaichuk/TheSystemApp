package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.components.SystemCutCornerShape
import com.ihor.thesystem.core.ui.components.SystemHoodBadge
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemLargePanelShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.domain.model.CalendarDayCompletionStatus
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarDayUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarUiState
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private val UKRAINIAN_LOCALE = Locale("uk", "UA")

@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors
    val selectedDay = remember(uiState.days, uiState.selectedDate) {
        uiState.selectedDate?.let { selectedDate ->
            uiState.days.firstOrNull { it.date == selectedDate }
        } ?: uiState.days.firstOrNull { it.isToday }
    }

    RefreshOnResume(viewModel::refreshVisibleData)

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
                .testTag(SystemUiTestTags.CALENDAR_SCROLL)
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalendarHeader(
                uiState = uiState,
                onOpenSettings = { navController.navigate(Routes.CalendarSettings) }
            )

            if (uiState.isLoading && uiState.days.isEmpty()) {
                CalendarLoadingBlock()
            } else {
                MonthOverviewPanel(
                    uiState = uiState,
                    onPreviousMonth = { viewModel.onMonthChange(uiState.currentMonth.minusMonths(1)) },
                    onNextMonth = { viewModel.onMonthChange(uiState.currentMonth.plusMonths(1)) },
                    onDateSelected = viewModel::onDateSelected
                )
                SelectedDayDetailsPanel(
                    uiState = uiState,
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
    uiState: CalendarUiState,
    onOpenSettings: () -> Unit
) {
    val activeDays = uiState.days.count {
        it.completionStatus == CalendarDayCompletionStatus.COMPLETED ||
            it.completionStatus == CalendarDayCompletionStatus.PARTIAL
    }
    val plannedTrainings = uiState.days.count { it.hasTrainingPlan }
    val missedDays = uiState.days.count { it.completionStatus == CalendarDayCompletionStatus.MISSED }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(SystemUiTestTags.CALENDAR_HEADER)
            .padding(bottom = 5.dp)
    ) {
        val compact = maxWidth < 400.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemHoodBadge(modifier = Modifier.size(64.dp))
                    CalendarHeaderTitle(modifier = Modifier.weight(1f))
                    GlassIconButton(
                        icon = Icons.Filled.Today,
                        onClick = onOpenSettings,
                        active = true
                    )
                }
                CalendarHeaderStats(activeDays, plannedTrainings, missedDays)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SystemHoodBadge(modifier = Modifier.size(76.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CalendarHeaderTitle()
                    CalendarHeaderStats(activeDays, plannedTrainings, missedDays)
                }
                GlassIconButton(
                    icon = Icons.Filled.Today,
                    onClick = onOpenSettings,
                    active = true
                )
            }
        }
    }
}

@Composable
private fun CalendarHeaderTitle(modifier: Modifier = Modifier) {
    val colors = SystemTheme.colors
    Text(
        text = "КАЛЕНДАР",
        style = MaterialTheme.typography.headlineLarge.copy(
            color = colors.textPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            lineHeight = 32.sp,
            letterSpacing = 1.0.sp
        ),
        modifier = modifier,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun CalendarHeaderStats(
    activeDays: Int,
    plannedTrainings: Int,
    missedDays: Int,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarHeaderStat(
            value = activeDays.toString(),
            label = "активних днів",
            modifier = Modifier.weight(1f)
        )
        HeaderDot(colors.accentPrimary)
        CalendarHeaderStat(
            value = plannedTrainings.toString(),
            label = "тренувань",
            accent = colors.accentPrimary,
            modifier = Modifier.weight(1f)
        )
        HeaderDot(colors.accentWarning)
        CalendarHeaderStat(
            value = missedDays.toString(),
            label = "пропуски",
            accent = colors.accentWarning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalendarHeaderStat(
    value: String,
    label: String,
    accent: Color = SystemTheme.colors.textSecondary,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HeaderDot(color: Color) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.86f))
    )
}

@Composable
private fun CalendarLoadingBlock() {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.accentPrimary)
        }
    }
}

@Composable
private fun CalendarRpgPanel(
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accent: Color = SystemTheme.colors.accentPrimary,
    contentPadding: Dp = SystemCardPadding,
    content: @Composable () -> Unit
) {
    val shape = systemLargePanelShape()
    Box(
        modifier = modifier
            .techSurface(
                shape = shape,
                active = active,
                accent = accent,
                role = TechSurfaceRole.Panel
            )
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
private fun MonthOverviewPanel(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val colors = SystemTheme.colors
    CalendarRpgPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalendarMonthArrowButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onPreviousMonth
                )
                Text(
                    text = uiState.currentMonth.toLocalizedMonthYear().uppercase(UKRAINIAN_LOCALE),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 1.8.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                CalendarMonthArrowButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNextMonth
                )
            }
            CalendarGrid(
                days = uiState.days,
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected
            )
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
    val colors = SystemTheme.colors
    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val dayModels = remember(days) { days.associateBy { it.date } }
    val startOffset = currentMonth.atDay(1).dayOfWeek.value - 1
    val gridStart = currentMonth.atDay(1).minusDays(startOffset.toLong())
    val rowCount = ((startOffset + currentMonth.lengthOfMonth() + 6) / 7).coerceAtLeast(5)

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        repeat(rowCount) { row ->
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
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
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
    val colors = SystemTheme.colors
    val enabled = isInCurrentMonth && model != null
    val shape = SystemCutCornerShape(8.dp)
    val isToday = model?.isToday == true
    val isMissed = model?.completionStatus == CalendarDayCompletionStatus.MISSED
    val shouldOutline = isSelected || isToday || isMissed
    val outlineColor = when {
        isSelected -> colors.accentPrimary
        isToday -> colors.accentPrimary.copy(alpha = 0.72f)
        isMissed -> colors.accentError
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clickable(enabled = enabled) { onDateSelected(date) }
    ) {
        if (shouldOutline) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(if (isSelected) 38.dp else 34.dp)
                    .techSurface(
                        shape = shape,
                        active = isInCurrentMonth,
                        accent = outlineColor,
                        role = TechSurfaceRole.Plate
                    )
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                color = when {
                    !isInCurrentMonth -> colors.textMuted.copy(alpha = 0.32f)
                    isSelected || isToday -> colors.accentPrimary
                    isMissed -> colors.textPrimary
                    else -> colors.textPrimary
                },
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        model?.let { day ->
            val dotColor = day.calendarDotColor()
            if (dotColor != Color.Transparent) {
                StatusDot(
                    color = dotColor,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        drawCircle(color = color.copy(alpha = 0.9f), radius = 2.8.dp.toPx())
    }
}

@Composable
private fun CalendarDayUiModel.calendarDotColor(): Color {
    val colors = SystemTheme.colors
    return when {
        completionStatus == CalendarDayCompletionStatus.COMPLETED -> colors.accentSuccess
        completionStatus == CalendarDayCompletionStatus.PARTIAL -> colors.accentWarning
        completionStatus == CalendarDayCompletionStatus.PLANNED -> colors.accentPrimary
        hasTrainingPlan -> colors.accentPrimary
        completionStatus == CalendarDayCompletionStatus.MISSED -> Color.Transparent
        else -> Color.Transparent
    }
}

@Composable
private fun SelectedDayDetailsPanel(
    uiState: CalendarUiState,
    day: CalendarDayUiModel?,
    onOpenDay: () -> Unit
) {
    val colors = SystemTheme.colors
    CalendarRpgPanel(
        modifier = Modifier.fillMaxWidth(),
        active = false,
        contentPadding = 14.dp
    ) {
        if (day == null) {
            Text(
                text = "Оберіть день у поточному місяці",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
            )
            return@CalendarRpgPanel
        }

        val hasLogs = uiState.dailyLogs.isNotEmpty() || uiState.workoutResults.isNotEmpty()
        val hasAnyPlan = day.hasTrainingPlan || day.totalTasks > 0 || hasLogs
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "ПОДІЇ НА ${day.date.toEventHeaderDate()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 1.8.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!day.isCalendarCycleConfigured) {
                Text(
                    text = "Налаштуйте календарний цикл, щоб бачити тут свої результати.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                )
                return@Column
            }

            if (!hasAnyPlan) {
                CalendarEmptyEventRow(
                    title = "Нічого не заплановано",
                    subtitle = "День без тренування і задач",
                    accent = colors.textMuted
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (day.hasTrainingPlan) {
                    CalendarEventRow(
                        title = day.plannedWorkoutName ?: "Тренування",
                        subtitle = "Планове тренування",
                        meta = "${day.plannedExerciseCount} вправ",
                        icon = Icons.Filled.FitnessCenter,
                        accent = colors.accentAi
                    )
                }
                if (day.totalTasks > 0) {
                    CalendarEventRow(
                        title = "To-do",
                        subtitle = "${day.completedTasks}/${day.totalTasks} виконано",
                        meta = day.completionStatus.toDisplayText(),
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        accent = colors.accentSuccess
                    )
                }
                if (hasLogs) {
                    CalendarEventRow(
                        title = "Логи дня",
                        subtitle = "${uiState.workoutResults.size} результатів · ${uiState.dailyLogs.size} записів",
                        meta = "Факт",
                        icon = Icons.AutoMirrored.Filled.Notes,
                        accent = colors.accentPrimary
                    )
                }
            }

            CalendarActionButton(
                text = "ВІДКРИТИ ДЕНЬ",
                icon = if (day.hasTrainingPlan) Icons.Filled.FitnessCenter else Icons.Filled.Today,
                onClick = onOpenDay,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalendarEmptyEventRow(
    title: String,
    subtitle: String,
    accent: Color
) {
    val colors = SystemTheme.colors
    val shape = SystemCutCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .techSurface(
                shape = shape,
                active = false,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                color = accent,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun CalendarEventRow(
    title: String,
    subtitle: String,
    meta: String,
    icon: ImageVector,
    accent: Color
) {
    val colors = SystemTheme.colors
    val shape = SystemCutCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .techSurface(
                shape = shape,
                active = false,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(accent.copy(alpha = 0.78f))
        )
        com.ihor.thesystem.core.ui.components.SystemHexIcon(
            icon = icon,
            accent = accent,
            modifier = Modifier.size(52.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = meta,
            style = MaterialTheme.typography.labelLarge.copy(
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CalendarActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = SystemCutCornerShape(12.dp)
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .techSurface(
                shape = shape,
                active = true,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Button
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accentPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.3.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CalendarMonthArrowButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(34.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    active: Boolean = false
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .techSurface(
                shape = shape,
                active = active,
                accent = colors.accentPrimary,
                role = if (active) TechSurfaceRole.Button else TechSurfaceRole.Plate
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) colors.accentPrimary else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun CalendarDayCompletionStatus.toDisplayText(): String =
    when (this) {
        CalendarDayCompletionStatus.COMPLETED -> "Виконано"
        CalendarDayCompletionStatus.PARTIAL -> "Частково"
        CalendarDayCompletionStatus.MISSED -> "Пропущено"
        CalendarDayCompletionStatus.PLANNED -> "Заплановано"
        CalendarDayCompletionStatus.NO_DATA -> "Немає даних"
    }

private fun YearMonth.toLocalizedMonthYear(): String {
    return "${monthValue.toUkrainianMonthName()} $year"
}

private fun LocalDate.toEventHeaderDate(): String {
    return "$dayOfMonth ${monthValue.toUkrainianMonthGenitive()}".uppercase(UKRAINIAN_LOCALE)
}

private fun Int.toUkrainianMonthName(): String =
    when (this) {
        1 -> "Січень"
        2 -> "Лютий"
        3 -> "Березень"
        4 -> "Квітень"
        5 -> "Травень"
        6 -> "Червень"
        7 -> "Липень"
        8 -> "Серпень"
        9 -> "Вересень"
        10 -> "Жовтень"
        11 -> "Листопад"
        else -> "Грудень"
    }

private fun Int.toUkrainianMonthGenitive(): String =
    when (this) {
        1 -> "січня"
        2 -> "лютого"
        3 -> "березня"
        4 -> "квітня"
        5 -> "травня"
        6 -> "червня"
        7 -> "липня"
        8 -> "серпня"
        9 -> "вересня"
        10 -> "жовтня"
        11 -> "листопада"
        else -> "грудня"
    }
