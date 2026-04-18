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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarDayUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.feature.calendar.viewmodel.WorkoutResultUiModel
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
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
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "КОНТРОЛЬ ЦИКЛУ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        )
                    )
                    uiState.todayInfo?.let { today ->
                        Text(
                            text = "СЬОГОДНІ: ${today.label.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when(today.cycleDay) {
                                    1 -> NeonGold
                                    2 -> NeonCyan
                                    else -> Color.White.copy(alpha = 0.3f)
                                },
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Surface(
                    color = NeonCyan.copy(alpha = 0.1f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f))
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
            }

            // ── Month Selector ────────────────────────────────────────
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.onMonthChange(uiState.currentMonth.minusMonths(1)) },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = uiState.currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("uk")).uppercase() + 
                               " ${uiState.currentMonth.year}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = RajdhaniFamily
                        )
                    )
                    IconButton(
                        onClick = { viewModel.onMonthChange(uiState.currentMonth.plusMonths(1)) },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

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
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(HexagonShape)
            .background(if (isSelected) NeonCyan.copy(0.15f) else PanelSurface.copy(alpha = 0.4f))
            .drawBehind {
                val path = buildHexagonPath(size)
                when (model.cycleDay) {
                    1 -> {
                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(NeonGold.copy(alpha = 0.4f), Color.Transparent),
                                center = center,
                                radius = size.width / 1.2f
                            )
                        )
                    }
                    2 -> {
                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(NeonCyan.copy(alpha = pulseAlpha), Color.Transparent),
                                center = center,
                                radius = size.width / 1.2f
                            )
                        )
                    }
                }
                val borderColor = when {
                    isSelected -> NeonCyan
                    model.isToday -> NeonCyanDim
                    else -> PanelBorder.copy(alpha = 0.5f)
                }
                drawPath(path, borderColor, style = Stroke(width = 1.dp.toPx()))
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = model.date.dayOfMonth.toString(),
            color = when {
                model.isToday -> NeonCyan
                model.cycleDay == 1 -> NeonGold
                model.cycleDay == 2 -> NeonCyan
                else -> TextPrimary
            },
            fontSize = 14.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = if (model.isToday) FontWeight.Bold else FontWeight.Normal
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sciPanel(NeonCyan.copy(0.3f), PanelSurface, 12.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ДЕНЬ ${date.dayOfMonth}.${date.monthValue}",
                    color = TextPrimary,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold
                )
                if (loggedWeight != null) {
                    Text(
                        text = "Вага: $loggedWeight кг",
                        color = NeonCyan,
                        fontFamily = RajdhaniFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Text("[X]", color = NeonRed, fontSize = 10.sp, fontFamily = RajdhaniFamily)
            }
        }
        
        Text(
            text = "СТАТУС: ${dayModel.label.uppercase()}",
            color = when(dayModel.cycleDay) {
                1 -> NeonGold
                2 -> NeonCyan
                else -> NeonGreen
            },
            fontSize = 11.sp,
            fontFamily = RajdhaniFamily,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        HorizontalDivider(color = NeonCyan.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        if (results.isNotEmpty()) {
            Text("РЕЗУЛЬТАТИ ТРЕНУВАННЯ:", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFamily)
            Spacer(Modifier.height(8.dp))
            results.forEach { result ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(result.exerciseName.uppercase(), color = NeonGold, fontSize = 11.sp, fontFamily = RajdhaniFamily)
                        val setsText = result.sets.joinToString(" | ") { "${it.weight}кг x ${it.reps}" }
                        Text(setsText, color = TextSecondary, fontSize = 10.sp, fontFamily = RajdhaniFamily, modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    MaxOneRepMaxText(
                        sets = result.sets.map { it.weight to it.reps },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            HorizontalDivider(color = NeonCyan.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        }

        // --- Блок Рекомендацій від Системи ---
        if (recommendations.isNotEmpty()) {
            Text(
                text = "РЕКОМЕНДАЦІЇ ВІД СИСТЕМИ:",
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RajdhaniFamily
            )
            Spacer(Modifier.height(8.dp))
            
            recommendations.forEach { rec ->
                Text(
                    text = "• ${rec.exerciseName}: ${rec.nextRecommendedWeight}кг, ${rec.nextRecommendedSets}x, ${rec.nextRecommendedReps}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = RajdhaniFamily,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
            HorizontalDivider(color = NeonCyan.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        }

        schedule?.let { data ->
            if (data.workoutTemplateName != null) {
                Text(
                    text = "ПЛАНОВА ПРОГРАМА: ${data.workoutTemplateName}",
                    color = NeonGold,
                    fontSize = 12.sp,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                data.exercises.forEach { exercise ->
                    Text(
                        text = "• ${exercise.name}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = RajdhaniFamily,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "РЕЖИМ: АКТИВНЕ ВІДНОВЛЕННЯ",
                    color = NeonCyanDim,
                    fontSize = 12.sp,
                    fontFamily = RajdhaniFamily
                )
            }
        }
    }
}

val HexagonShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(buildHexagonPath(size))
    }
}
