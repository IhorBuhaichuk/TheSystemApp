package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.MaxOneRepMaxText
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarDayUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.feature.calendar.viewmodel.WorkoutResultUiModel
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020408))) {
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
                        recommendations = uiState.nextWorkoutRecommendations,
                        loggedWeight = uiState.loggedWeightForDate,
                        onDismiss = { viewModel.onDateSelected(null) },
                        viewModel = viewModel
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
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
            text = "КАЛЕНДАР",
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
    val infiniteTransition = rememberInfiniteTransition()
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF020408))
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.1f + (size.height * 0.2f * colorShift)),
                radius = 800.dp.toPx()
            )
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonGold.copy(alpha = 0.03f), Color.Transparent),
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
    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
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
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick() }
            .drawBehind {
                val baseColor = when (model.cycleDay) {
                    1 -> NeonGold
                    2 -> NeonCyan
                    else -> Color.White.copy(alpha = 0.08f)
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
                        color = NeonCyan.copy(alpha = 0.1f),
                        radius = size.width / 2f
                    )
                }

                if (model.isToday && !isSelected) {
                    drawCircle(
                        color = NeonCyan,
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
                model.isToday -> NeonCyan
                model.cycleDay == 1 -> NeonGold
                model.cycleDay == 2 -> NeonCyan
                else -> TextPrimary.copy(alpha = 0.6f)
            },
            fontSize = 14.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = if (model.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DailyScheduleSection(
    date: LocalDate,
    dayModel: CalendarDayUiModel,
    results: List<WorkoutResultUiModel>,
    recommendations: List<ProgressionMatrixEntry>,
    loggedWeight: Double?,
    onDismiss: () -> Unit,
    viewModel: CalendarViewModel
) {
    val schedule by viewModel.getScheduleForDay(dayModel.cycleDay).collectAsState(null)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF0F0F1A)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(NeonCyan.copy(0.2f), NeonGold.copy(0.1f))),
                RoundedCornerShape(32.dp)
            )
    ) {
        // Decorative glow
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .blur(50.dp)
                .background(
                    if (dayModel.cycleDay == 1) NeonGold.copy(0.1f) else NeonCyan.copy(0.1f),
                    CircleShape
                )
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "ДЕТАЛІ ДНЯ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "${date.dayOfMonth}.${date.monthValue}.${date.year}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = RajdhaniFamily
                        )
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null, 
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when(dayModel.cycleDay) {
                                1 -> NeonGold
                                2 -> NeonCyan
                                else -> NeonGreen
                            }
                        )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "СТАТУС: ${dayModel.label.uppercase()}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            if (loggedWeight != null) {
                Text(
                    text = "ВАГА: $loggedWeight КГ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonCyan.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(start = 18.dp, top = 4.dp)
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.05f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            if (results.isNotEmpty()) {
                Text(
                    text = "РЕЗУЛЬТАТИ ТРЕНУВАННЯ",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
                Spacer(Modifier.height(12.dp))
                results.forEach { result ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(result.exerciseName.uppercase(), color = NeonGold, style = MaterialTheme.typography.labelMedium, fontFamily = RajdhaniFamily)
                            val setsText = result.sets.joinToString(" | ") { "${it.weight}кг x ${it.reps}" }
                            Text(setsText, color = TextSecondary, fontSize = 11.sp, fontFamily = RajdhaniFamily)
                        }
                        MaxOneRepMaxText(
                            sets = result.sets.map { it.weight to it.reps },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            }

            if (recommendations.isNotEmpty()) {
                Text(
                    text = "РЕКОМЕНДАЦІЇ СИСТЕМИ",
                    style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
                Spacer(Modifier.height(8.dp))
                recommendations.forEach { rec ->
                    Text(
                        text = "• ${rec.exerciseName}: ${rec.nextRecommendedWeight}кг, ${rec.nextRecommendedSets}x, ${rec.nextRecommendedReps}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = RajdhaniFamily,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            }

            schedule?.let { data ->
                val templateName = data.workoutTemplateName
                if (templateName != null) {
                    Text(
                        text = "ПЛАНОВА ПРОГРАМА",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Text(
                        text = templateName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFamily)
                    )
                    Spacer(Modifier.height(12.dp))
                    data.exercises.forEach { exercise ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(NeonGold.copy(0.4f)))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontFamily = RajdhaniFamily)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "РЕЖИМ: АКТИВНЕ ВІДНОВЛЕННЯ",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = NeonCyanDim,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFamily
                        )
                    )
                }
            }
        }
    }
}


