package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihor.thesystem.R
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarDayUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarUiState
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.feature.calendar.viewmodel.DailyTaskSnapshotUiModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
import com.ihor.thesystem.feature.status.ui.components.workout.CycleDaySelector
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val CalendarBlue = Color(0xFF6EA8FF)
private val CalendarCyan = Color(0xFF67E8F9)
private val CalendarViolet = Color(0xFF8B5CF6)
private val CalendarGold = Color(0xFFFFC978)
private val CalendarGreen = Color(0xFF76F0A2)
private val CalendarRed = Color(0xFFFF6B76)
private val CalendarSurface = Color(0xFF07111F)
private val CalendarPanel = Color(0xFF0A1424)
private val CalendarPanelSoft = Color(0xFF101B2D)
private val CalendarLine = Color(0xFF25334A)
private val CalendarMuted = Color(0xFF8FA0B9)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02050B))
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CalendarProfileBar(uiState)

            CalendarTitleRow(uiState)

            MonthCalendarPanel(
                uiState = uiState,
                onMonthChange = viewModel::onMonthChange,
                onDateClick = viewModel::onDateSelected
            )

            uiState.selectedDate?.let { date ->
                val selectedDayModel = uiState.days.find { it.date == date }
                    ?: CalendarDayUiModel(
                        date = date,
                        cycleDay = uiState.todayInfo?.cycleDay ?: 1,
                        label = uiState.todayInfo?.label.orEmpty(),
                        isToday = false
                    )

                FocusedDayOverviewPanel(
                    date = date,
                    dayModel = selectedDayModel,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            CycleControlPanel(uiState, viewModel)
        }
    }
}

@Composable
private fun CalendarProfileBar(uiState: CalendarUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarPreview(avatarUri = uiState.avatarUri)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = uiState.playerName.ifBlank { stringResource(R.string.app_name) },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    lineHeight = 22.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = rankTitle(uiState.playerRank),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CalendarBlue,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = CalendarViolet,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        StreakPill(days = uiState.currentStreak)
    }
}

@Composable
private fun AvatarPreview(avatarUri: String?) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                Brush.radialGradient(
                    listOf(CalendarBlue.copy(alpha = 0.34f), Color.Transparent)
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.24f), CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF07101D))
                    .border(1.dp, CalendarBlue.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = CalendarBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun StreakPill(days: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CalendarGold.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, CalendarGold.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = CalendarGold,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "СЕРІЯ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.74f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Text(
                text = "$days ДН",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = CalendarGold,
                    fontWeight = FontWeight.Black
                )
            )
        }
    }
}

@Composable
private fun CalendarTitleRow(uiState: CalendarUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "КАЛЕНДАР",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 25.sp
                )
            )
            Text(
                text = "Відстежуй прогрес. Послідовність вирішує.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CalendarMuted,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        CommandMetricPill(
            icon = Icons.Filled.Star,
            text = "${uiState.xpThisWeek} XP",
            tint = CalendarBlue
        )
    }
}

@Composable
private fun CommandMetricPill(
    icon: ImageVector,
    text: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.26f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.11f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun MonthCalendarPanel(
    uiState: CalendarUiState,
    onMonthChange: (YearMonth) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    RpgGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        corner = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MonthSwitcher(
                currentMonth = uiState.currentMonth,
                onMonthChange = onMonthChange
            )

            CalendarGrid(
                days = uiState.days,
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
private fun MonthSwitcher(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onMonthChange(currentMonth.minusMonths(1)) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = CalendarMuted,
                modifier = Modifier.size(22.dp)
            )
        }

        val monthText = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("uk"))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("uk")) else it.toString() }
        Text(
            text = "$monthText ${currentMonth.year}",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        )

        IconButton(
            onClick = { onMonthChange(currentMonth.plusMonths(1)) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CalendarMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDayUiModel>,
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val locale = Locale("uk")
    val weekDays = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "НД")
    val currentDays = remember(days) { days.associateBy { it.date } }
    val startOffset = currentMonth.atDay(1).dayOfWeek.value - 1
    val gridStart = currentMonth.atDay(1).minusDays(startOffset.toLong())

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    color = CalendarMuted.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        repeat(6) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(7) { col ->
                    val date = gridStart.plusDays((row * 7 + col).toLong())
                    val dayModel = currentDays[date]
                    val isInCurrentMonth = date.month == currentMonth.month
                    QuietCalendarDayCell(
                        date = date,
                        model = dayModel,
                        isInCurrentMonth = isInCurrentMonth,
                        isSelected = date == selectedDate,
                        onClick = { if (isInCurrentMonth) onDateClick(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuietCalendarDayCell(
    date: LocalDate,
    model: CalendarDayUiModel?,
    isInCurrentMonth: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cycleColor = cycleDayColor(model?.cycleDay)
    val hasShiftColor = cycleColor != Color.Transparent
    val selectionColor = if (hasShiftColor) cycleColor else CalendarBlue
    val textColor = when {
        !isInCurrentMonth -> Color.White.copy(alpha = 0.2f)
        isSelected -> Color.White
        model?.isToday == true && hasShiftColor -> cycleColor.copy(alpha = 0.9f)
        model?.isToday == true -> Color.White.copy(alpha = 0.92f)
        hasShiftColor -> cycleColor.copy(alpha = 0.72f)
        else -> Color.White.copy(alpha = 0.66f)
    }

    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = isInCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                selectionColor.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.035f)
                            )
                        )
                    )
                    .border(1.dp, selectionColor.copy(alpha = 0.42f), RoundedCornerShape(10.dp))
            )
        }

        Box(
            modifier = Modifier.size(if (isSelected) 44.dp else 37.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isInCurrentMonth && model != null && hasShiftColor) {
                    drawCircle(
                        color = cycleColor.copy(alpha = if (isSelected) 0.16f else 0.09f),
                        radius = size.minDimension / 2.02f
                    )
                    drawCircle(
                        color = cycleColor.copy(alpha = if (isSelected) 0.2f else 0.11f),
                        radius = size.minDimension / 2.95f
                    )
                }

                drawCircle(
                    color = if (isInCurrentMonth) Color.White.copy(alpha = 0.052f) else Color.White.copy(alpha = 0.032f),
                    radius = size.minDimension / 2.25f
                )

                if (model?.isToday == true) {
                    drawCircle(
                        color = selectionColor.copy(alpha = 0.42f),
                        radius = size.minDimension / 2.1f,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                }
            }

            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = if (isSelected) 15.sp else 14.sp,
                fontWeight = if (isSelected || model?.isToday == true) FontWeight.Black else FontWeight.SemiBold,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    model: CalendarDayUiModel?,
    isInCurrentMonth: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = remember(model, isInCurrentMonth) { dayStatus(model, isInCurrentMonth) }
    val statusColor = status.color
    val cycleColor = cycleDayColor(model?.cycleDay)
    val progress = if (model != null && model.totalTasks > 0) {
        model.completedTasks.toFloat() / model.totalTasks
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(520, easing = EaseOutCubic),
        label = "day_progress"
    )

    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isInCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(cycleColor.copy(alpha = 0.36f), CalendarViolet.copy(alpha = 0.16f))
                        )
                    )
                    .border(1.dp, cycleColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            )
        }

        Box(
            modifier = Modifier.size(if (isSelected) 45.dp else 38.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isInCurrentMonth && model != null) {
                    drawCircle(
                        color = cycleColor.copy(alpha = if (isSelected) 0.22f else 0.13f),
                        radius = size.minDimension / 2.02f
                    )
                    drawCircle(
                        color = cycleColor.copy(alpha = if (isSelected) 0.28f else 0.17f),
                        radius = size.minDimension / 2.95f
                    )
                }

                drawCircle(
                    color = if (isInCurrentMonth) Color.White.copy(alpha = 0.055f) else Color.White.copy(alpha = 0.035f),
                    radius = size.minDimension / 2.25f
                )
                if (status != CalendarDayStatus.Empty) {
                    drawArc(
                        color = statusColor.copy(alpha = if (isSelected) 0.95f else 0.72f),
                        startAngle = -90f,
                        sweepAngle = if (status == CalendarDayStatus.Missed) 360f else 360f * animatedProgress.coerceAtLeast(0.16f),
                        useCenter = false,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = when {
                        !isInCurrentMonth -> Color.White.copy(alpha = 0.22f)
                        isSelected -> Color.White
                        model?.isToday == true -> cycleColor
                        model != null -> cycleColor.copy(alpha = 0.92f)
                        else -> Color.White.copy(alpha = 0.78f)
                    },
                    fontSize = if (isSelected) 15.sp else 14.sp,
                    fontWeight = if (isSelected || model?.isToday == true) FontWeight.Black else FontWeight.SemiBold,
                    lineHeight = 15.sp
                )
                if (isSelected) {
                    Text(
                        text = "СЬОГОДНІ".takeIf { model?.isToday == true } ?: "${model?.completedTasks ?: 0}/${model?.totalTasks ?: 0}",
                        color = CalendarCyan,
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                } else if (model != null && model.totalTasks > 0) {
                    Text(
                        text = "${model.completedTasks}/${model.totalTasks}",
                        color = statusColor.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("День", "цикл 1", cycleDayColor(1))
                LegendItem("Ніч", "цикл 2", cycleDayColor(2))
                LegendItem("Відн.", "цикл 3+", cycleDayColor(3))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("Виконано", "усі задачі", CalendarViolet)
                LegendItem("Частково", "є прогрес", CalendarCyan)
                LegendItem("Пропуск", "0 задач", CalendarRed)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, subtitle: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(2.dp, color, CircleShape)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CalendarMuted,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun FocusedDayOverviewPanel(
    date: LocalDate,
    dayModel: CalendarDayUiModel,
    uiState: CalendarUiState,
    viewModel: CalendarViewModel
) {
    val schedule by viewModel.getScheduleForDay(dayModel.cycleDay).collectAsState(null)
    val plannedExercises = schedule?.exercises.orEmpty()
    val completedExerciseIds = remember(uiState.workoutResults) {
        uiState.workoutResults.map { it.exerciseId }.toSet()
    }
    val completedExercises = plannedExercises.count { completedExerciseIds.contains(it.id) }
    val workoutProgress = if (plannedExercises.isNotEmpty()) {
        completedExercises.toFloat() / plannedExercises.size
    } else {
        0f
    }

    RpgGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        corner = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ОГЛЯД ДНЯ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = dateLabel(date),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CalendarMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalendarPanelSoft.copy(alpha = 0.58f))
                    .border(1.dp, CalendarGold.copy(alpha = 0.11f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkoutProgressRing(
                    progress = workoutProgress,
                    completed = completedExercises,
                    total = plannedExercises.size
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (plannedExercises.isNotEmpty()) "Тренування дня" else "Режим дня",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = CalendarGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (plannedExercises.isNotEmpty()) {
                            "$completedExercises з ${plannedExercises.size} вправ виконано"
                        } else {
                            dayModel.label.ifBlank { "Тренування не заплановано" }
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.76f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    ThinProgressLine(progress = workoutProgress, color = CalendarGold.copy(alpha = 0.82f))
                }
            }

            if (plannedExercises.isNotEmpty()) {
                WorkoutExercisePlan(
                    title = schedule?.workoutTemplateName ?: "План тренування",
                    exercises = plannedExercises.map { it.name to completedExerciseIds.contains(it.id) }
                )
            } else {
                RecoveryPlanLine(label = dayModel.label)
            }

            if (uiState.loggedWeightForDate != null) {
                InfoLine(
                    icon = Icons.Filled.TrendingUp,
                    title = "Вага",
                    subtitle = "${uiState.loggedWeightForDate} кг",
                    tint = CalendarGreen
                )
            }

            DailyQuestScroll(uiState.dailyTaskSnapshot)
        }
    }
}

@Composable
private fun WorkoutProgressRing(
    progress: Float,
    completed: Int,
    total: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = EaseOutExpo),
        label = "workout_ring"
    )

    Box(
        modifier = Modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = CalendarGold.copy(alpha = 0.88f),
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeWidth = 7.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    lineHeight = 24.sp
                )
            )
            Text(
                text = if (total > 0) "$completed/$total" else "ВПРАВИ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun WorkoutExercisePlan(
    title: String,
    exercises: List<Pair<String, Boolean>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.13f))
            .border(1.dp, CalendarGold.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoLine(
            icon = Icons.Filled.FitnessCenter,
            title = "План тренування",
            subtitle = title,
            tint = CalendarGold,
            compact = true
        )

        exercises.forEach { (name, isCompleted) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) CalendarGreen.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.045f))
                        .border(
                            1.dp,
                            if (isCompleted) CalendarGreen.copy(alpha = 0.66f) else Color.White.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = CalendarGreen, modifier = Modifier.size(13.dp))
                    }
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = if (isCompleted) 0.48f else 0.78f),
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DailyQuestScroll(snapshot: DailyTaskSnapshotUiModel) {
    if (!snapshot.hasAnyData) return

    val scrollText = Color(0xFF332816)
    val paper = Color(0xFFD8C493)
    val paperDark = Color(0xFFBFA875)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(paper.copy(alpha = 0.96f), paperDark.copy(alpha = 0.94f))
                )
            )
            .border(1.dp, Color(0xFF78643D).copy(alpha = 0.46f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = "Завдання дня",
            color = scrollText.copy(alpha = 0.88f),
            fontFamily = FontFamily.Cursive,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        snapshot.completedTasks.forEach { task ->
            ScrollTaskLine(taskName = task, isCompleted = true, textColor = scrollText)
        }
        snapshot.failedTasks.forEach { task ->
            ScrollTaskLine(taskName = task, isCompleted = false, textColor = scrollText)
        }
    }
}

@Composable
private fun ScrollTaskLine(
    taskName: String,
    isCompleted: Boolean,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            if (isCompleted) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF1F7A45), modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = taskName,
            modifier = Modifier.weight(1f),
            color = textColor.copy(alpha = if (isCompleted) 0.58f else 0.88f),
            fontFamily = FontFamily.Cursive,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DayOverviewPanel(
    date: LocalDate,
    dayModel: CalendarDayUiModel,
    uiState: CalendarUiState,
    viewModel: CalendarViewModel
) {
    val schedule by viewModel.getScheduleForDay(dayModel.cycleDay).collectAsState(null)
    val progress = if (uiState.dailyTaskSnapshot.hasAnyData) {
        uiState.dailyTaskSnapshot.completedPercent / 100f
    } else if (dayModel.totalTasks > 0) {
        dayModel.completedTasks.toFloat() / dayModel.totalTasks
    } else {
        0f
    }

    RpgGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        corner = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ОГЛЯД ДНЯ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = dateLabel(date),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CalendarMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalendarPanelSoft.copy(alpha = 0.64f))
                    .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressRing(progress = progress)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Прогрес дня",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = CalendarBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${dayModel.completedTasks} з ${dayModel.totalTasks} задач виконано",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.76f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    ThinProgressLine(progress = progress, color = CalendarViolet)
                }
            }

            DailyTasksList(uiState.dailyTaskSnapshot)

            if (schedule?.exercises?.isNotEmpty() == true) {
                CompactWorkoutPlan(
                    title = schedule?.workoutTemplateName ?: "План тренування",
                    exercises = schedule?.exercises?.map { it.name }.orEmpty()
                )
            } else {
                RecoveryPlanLine(label = dayModel.label)
            }

            if (uiState.loggedWeightForDate != null) {
                InfoLine(
                    icon = Icons.Filled.TrendingUp,
                    title = "Вага",
                    subtitle = "${uiState.loggedWeightForDate} кг",
                    tint = CalendarGreen
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = EaseOutExpo),
        label = "overview_ring"
    )

    Box(
        modifier = Modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = CalendarViolet,
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeWidth = 7.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    lineHeight = 24.sp
                )
            )
            Text(
                text = "ГОТОВО",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun DailyTasksList(snapshot: DailyTaskSnapshotUiModel) {
    if (!snapshot.hasAnyData) {
        EmptyOverviewLine()
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        snapshot.completedTasks.forEach { taskName ->
            TaskOverviewRow(taskName = taskName, isCompleted = true)
        }
        snapshot.failedTasks.forEach { taskName ->
            TaskOverviewRow(taskName = taskName, isCompleted = false)
        }
    }
}

@Composable
private fun TaskOverviewRow(taskName: String, isCompleted: Boolean) {
    val tint = if (isCompleted) CalendarViolet else CalendarMuted.copy(alpha = 0.45f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(CalendarPanelSoft.copy(alpha = 0.62f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuestBadge(
            icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
            tint = tint,
            size = 42.dp
        )
        Text(
            text = taskName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = if (isCompleted) 0.9f else 0.52f),
                fontWeight = FontWeight.Bold,
                textDecoration = if (isCompleted) null else TextDecoration.None
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(31.dp)
                .clip(CircleShape)
                .background(if (isCompleted) CalendarViolet.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                    1.dp,
                    if (isCompleted) CalendarViolet.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.13f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = CalendarViolet,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactWorkoutPlan(title: String, exercises: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.14f))
            .border(1.dp, CalendarGold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoLine(
            icon = Icons.Filled.FitnessCenter,
            title = "План тренування",
            subtitle = title,
            tint = CalendarGold,
            compact = true
        )
        exercises.take(4).forEach { name ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(CalendarGold.copy(alpha = 0.75f))
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (exercises.size > 4) {
            Text(
                text = "+ ще ${exercises.size - 4}",
                color = CalendarMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 13.dp)
            )
        }
    }
}

@Composable
private fun RecoveryPlanLine(label: String) {
    InfoLine(
        icon = Icons.Filled.Shield,
        title = "Режим дня",
        subtitle = label.ifBlank { "Активне відновлення" },
        tint = CalendarGreen
    )
}

@Composable
private fun EmptyOverviewLine() {
    InfoLine(
        icon = Icons.Filled.Today,
        title = "Задачі",
        subtitle = "На цей день немає збережених задач",
        tint = CalendarMuted
    )
}

@Composable
private fun InfoLine(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (compact) Modifier else Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(CalendarPanelSoft.copy(alpha = 0.62f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    .padding(13.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuestBadge(icon = icon, tint = tint, size = 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = tint,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CycleControlPanel(
    uiState: CalendarUiState,
    viewModel: CalendarViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    RpgGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        corner = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Today,
                        contentDescription = null,
                        tint = CalendarBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "ЦИКЛ ДНЯ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = uiState.todayInfo?.label ?: "Синхронізація",
                            style = MaterialTheme.typography.labelSmall.copy(color = CalendarMuted)
                        )
                    }
                }
                Text(
                    text = if (expanded) "СХОВАТИ" else "НАЛАШТУВАТИ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CalendarBlue,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            AnimatedVisibility(visible = expanded) {
                CycleDaySelector(
                    days = uiState.cycleDays,
                    onTap = { viewModel.onConfirmSync(it) },
                    onLongPress = { viewModel.onConfirmSync(it) }
                )
            }
        }
    }
}

@Composable
private fun RpgGlassPanel(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CalendarPanel.copy(alpha = 0.9f),
                        CalendarSurface.copy(alpha = 0.84f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
                            CalendarBlue.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                ),
                RoundedCornerShape(corner)
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
private fun QuestBadge(
    icon: ImageVector,
    tint: Color,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.22f), Color.Transparent)))
            .border(1.dp, tint.copy(alpha = 0.34f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.46f)
        )
    }
}

@Composable
private fun ThinProgressLine(progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(520, easing = EaseOutCubic),
        label = "calendar_thin_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(CircleShape)
            .background(CalendarLine.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(color)
        )
    }
}

private enum class CalendarDayStatus(val color: Color) {
    Completed(CalendarViolet),
    Partial(CalendarCyan),
    Missed(CalendarRed),
    Empty(Color.Transparent)
}

private fun cycleDayColor(cycleDay: Int?): Color = when (cycleDay) {
    1 -> Color(0xFFC7A86B)
    2 -> Color(0xFF6F86B8)
    else -> Color.Transparent
}

private fun dayStatus(model: CalendarDayUiModel?, isInCurrentMonth: Boolean): CalendarDayStatus {
    if (!isInCurrentMonth || model == null || model.totalTasks == 0) return CalendarDayStatus.Empty
    return when {
        model.completedTasks >= model.totalTasks -> CalendarDayStatus.Completed
        model.completedTasks > 0 -> CalendarDayStatus.Partial
        model.date.isBefore(LocalDate.now()) -> CalendarDayStatus.Missed
        else -> CalendarDayStatus.Empty
    }
}

@Composable
private fun rankTitle(rank: PlayerRank): String = when (rank) {
    PlayerRank.NOVICE -> stringResource(R.string.rank_novice)
    PlayerRank.APPRENTICE -> stringResource(R.string.rank_apprentice)
    PlayerRank.ADEPT -> stringResource(R.string.rank_adept)
    PlayerRank.EXPERT -> stringResource(R.string.rank_expert)
    PlayerRank.MASTER -> stringResource(R.string.rank_master)
    PlayerRank.THE_SYSTEM -> stringResource(R.string.rank_the_system)
}

private fun dateLabel(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.SHORT_STANDALONE, Locale("uk"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("uk")) else it.toString() }
    return "${date.dayOfMonth} $month ${date.year}"
}
