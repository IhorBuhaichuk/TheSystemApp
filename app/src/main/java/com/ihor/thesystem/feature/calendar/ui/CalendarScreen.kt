package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.MaxOneRepMaxText
import com.ihor.thesystem.core.ui.components.glassCard
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarDayUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.feature.calendar.viewmodel.DailyTaskSnapshotUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.WorkoutResultUiModel
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.usecase.CalendarLogItem
import com.ihor.thesystem.domain.usecase.LogType
import com.ihor.thesystem.feature.status.ui.components.workout.CycleDaySelector
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        // Shared dynamic background
        AnimatedCalendarBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            CycleDaySelector(
                days = uiState.cycleDays,
                onTap = { viewModel.onConfirmSync(it) },
                onLongPress = { viewModel.onConfirmSync(it) }
            )

            CalendarHeaderAndMonthSelector(
                currentMonth = uiState.currentMonth,
                onMonthChange = { viewModel.onMonthChange(it) }
            )

            CalendarLegend()
            Spacer(Modifier.height(16.dp))

            // ── Calendar Grid ─────────────────────────────────────────
            CalendarGrid(
                days = uiState.days,
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                onDateClick = { viewModel.onDateSelected(it) }
            )

            // ── Details Section ───────────────────────────────────────
            uiState.selectedDate?.let { date ->
                val selectedDayModel = uiState.days.find { it.date == date }
                if (selectedDayModel != null) {
                    Spacer(Modifier.height(24.dp))
                    DailyScheduleSection(
                        date = date,
                        dayModel = selectedDayModel,
                        results = uiState.workoutResults,
                        dailyLogs = uiState.dailyLogs,
                        taskSnapshot = uiState.dailyTaskSnapshot,
                        recommendations = uiState.nextWorkoutRecommendations,
                        loggedWeight = uiState.loggedWeightForDate,
                        onDismiss = { viewModel.onDateSelected(null) },
                        viewModel = viewModel
                    )
                }
            }

            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
private fun CalendarHeaderAndMonthSelector(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.text_calendar_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            val monthText = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("uk"))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("uk")) else it.toString() }
            
            Text(
                text = "$monthText ${currentMonth.year}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontFamily = RajdhaniFamily,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(
                onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedCalendarBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "calendar_bg")
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(BackgroundDeep)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Primary.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.1f + (size.height * 0.2f * colorShift)),
                radius = 800.dp.toPx()
            )
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(StatusWarning.copy(alpha = 0.03f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.9f - (size.height * 0.2f * colorShift)),
                radius = 700.dp.toPx()
            )
        )
    }
}

@Composable
fun CalendarGrid(
    days: List<CalendarDayUiModel>,
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val weekDays = listOf(
        stringResource(R.string.text_day_mon),
        stringResource(R.string.text_day_tue),
        stringResource(R.string.text_day_wed),
        stringResource(R.string.text_day_thu),
        stringResource(R.string.text_day_fri),
        stringResource(R.string.text_day_sat),
        stringResource(R.string.text_day_sun)
    )
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value 

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = RajdhaniFamily,
                    modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                )
            }
        }

        val totalCells = (firstDayOfWeek - 1) + currentMonth.lengthOfMonth()
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - (firstDayOfWeek - 2)

                    if (dayOfMonth in 1..currentMonth.lengthOfMonth()) {
                        val model = days.find { it.date.dayOfMonth == dayOfMonth }
                        Box(modifier = Modifier.weight(1f)) {
                            if (model != null) {
                                CalendarDayCell(
                                    model = model,
                                    isSelected = model.date == selectedDate,
                                    onClick = { onDateClick(model.date) }
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CalendarDayCell(
    model: CalendarDayUiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dayColor = StatusWarning
    val nightColor = Primary
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick() }
            .drawBehind {
                val baseColor = when (model.cycleDay) {
                    1 -> dayColor
                    2 -> nightColor
                    else -> OnBackground.copy(alpha = 0.08f)
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isSelected) baseColor.copy(alpha = 0.5f) else baseColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width / 1.6f
                    )
                )

                if (isSelected) {
                    drawCircle(
                        color = Primary.copy(alpha = 0.1f),
                        radius = size.width / 2f
                    )
                }

                if (model.isToday && !isSelected) {
                    drawCircle(
                        color = Primary,
                        radius = 2.dp.toPx(),
                        center = Offset(center.x, size.height * 0.85f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = model.date.dayOfMonth.toString(),
            color = when {
                model.isToday -> Primary
                model.cycleDay == 1 -> StatusWarning
                model.cycleDay == 2 -> Primary
                else -> OnBackground.copy(alpha = 0.6f)
            },
            fontSize = 14.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = if (model.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(label = "ДЕНЬ", color = StatusWarning)
        LegendItem(label = "НІЧ", color = Primary)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, color = color, fontSize = 10.sp, fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyScheduleSection(
    date: LocalDate,
    dayModel: CalendarDayUiModel,
    results: List<WorkoutResultUiModel>,
    dailyLogs: List<CalendarLogItem>,
    taskSnapshot: DailyTaskSnapshotUiModel,
    recommendations: List<ProgressionMatrixEntry>,
    loggedWeight: Double?,
    onDismiss: () -> Unit,
    viewModel: CalendarViewModel
) {
    val schedule by viewModel.getScheduleForDay(dayModel.cycleDay).collectAsState(null)
    var isExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.text_day_details),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "${date.dayOfMonth}.${date.monthValue}.${date.year}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = OnBackground,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = RajdhaniFamily
                        )
                    )
                }
                
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .background(OnBackground.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isActuallyWorkoutDay = schedule?.let { it.workoutTemplateId != null && it.exercises.isNotEmpty() } ?: false

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActuallyWorkoutDay) {
                                        when(dayModel.cycleDay) {
                                            1 -> StatusWarning
                                            2 -> Primary
                                            else -> StatusSuccess
                                        }
                                    } else {
                                        StatusSuccess
                                    }
                                )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isActuallyWorkoutDay) {
                                "${stringResource(R.string.text_status_label)}: ${dayModel.label.uppercase()}"
                            } else {
                                "${stringResource(R.string.text_status_label)}: ${stringResource(R.string.text_active_recovery).uppercase()}"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = OnBackground.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    if (loggedWeight != null) {
                        Text(
                            text = "${stringResource(R.string.text_label_weight)}: $loggedWeight ${stringResource(R.string.text_unit_kg)}".uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(start = 14.dp, top = 4.dp)
                        )
                    }

                    DailyTasksSnapshotSection(taskSnapshot)

                    HorizontalDivider(
                        color = OnBackground.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )

                    if (results.isNotEmpty() || dailyLogs.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.text_activity_day),
                            style = MaterialTheme.typography.labelSmall.copy(color = Primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        // Render Unified Logs (Quests + Completed Exercises)
                        dailyLogs.forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.title.uppercase(),
                                        color = if (log.type == LogType.WORKOUT) StatusWarning else Primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = log.subtitle,
                                        color = OnBackground,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = RajdhaniFamily
                                    )
                                }
                                if (log.isCompleted) {
                                    Icon(Icons.Default.CheckCircle, "Completed", tint = StatusSuccess, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (results.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            results.forEach { result ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 14.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(result.exerciseName.uppercase(), color = StatusWarning.copy(0.7f), style = MaterialTheme.typography.labelMedium, fontFamily = RajdhaniFamily)
                                        val setsText = result.sets.joinToString(" | ") { "${it.weight}кг x ${it.reps}" }
                                        Text(setsText, color = OnSurfaceVariant, fontSize = 11.sp, fontFamily = RajdhaniFamily)
                                    }
                                    MaxOneRepMaxText(
                                        sets = result.sets.map { it.weight to it.reps },
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = OnBackground.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    }

                    if (recommendations.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.text_system_recommendations),
                            style = MaterialTheme.typography.labelSmall.copy(color = Primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Spacer(Modifier.height(8.dp))
                        recommendations.forEach { rec ->
                            Text(
                                text = "• ${rec.exerciseName}: ${stringResource(R.string.text_workout_recommendation_format, rec.nextRecommendedSets ?: 1, rec.nextRecommendedReps ?: 0, rec.nextRecommendedWeight ?: 0f)}",
                                color = OnBackground,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = RajdhaniFamily,
                                modifier = Modifier.padding(start = 14.dp, bottom = 4.dp)
                            )
                        }
                        HorizontalDivider(color = OnBackground.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    }

                    schedule?.let { data ->
                        val templateName = data.workoutTemplateName
                        if (templateName != null && data.exercises.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.text_planned_program),
                                style = MaterialTheme.typography.labelSmall.copy(color = StatusWarning, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                            Text(
                                text = templateName.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(color = OnBackground, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFamily)
                            )
                            Spacer(Modifier.height(12.dp))
                            data.exercises.forEach { exercise ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    modifier = Modifier.padding(start = 14.dp, bottom = 6.dp)
                                ) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(StatusWarning.copy(0.4f)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant, fontFamily = RajdhaniFamily)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.text_active_recovery),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Primary.copy(0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFamily
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTasksSnapshotSection(
    snapshot: DailyTaskSnapshotUiModel
) {
    if (!snapshot.hasAnyData) return

    Column {
        HorizontalDivider(
            color = OnBackground.copy(alpha = 0.05f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Text(
            text = stringResource(R.string.text_daily_tasks_section).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
        Spacer(Modifier.height(16.dp))

        if (snapshot.completedTasks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.text_completed_tasks).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${snapshot.completedPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RajdhaniFamily
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            snapshot.completedTasks.forEach { taskName ->
                Row(
                    modifier = Modifier.padding(start = 14.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = taskName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnBackground.copy(alpha = 0.7f),
                            fontFamily = RajdhaniFamily
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (snapshot.failedTasks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.text_failed_tasks).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = StatusError,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${snapshot.failedPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = StatusError,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RajdhaniFamily
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            snapshot.failedTasks.forEach { taskName ->
                Row(
                    modifier = Modifier.padding(start = 14.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = taskName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnBackground.copy(alpha = 0.7f),
                            fontFamily = RajdhaniFamily
                        )
                    )
                }
            }
        }
    }
}
