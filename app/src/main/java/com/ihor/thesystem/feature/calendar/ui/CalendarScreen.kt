package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarViewModel
import com.ihor.thesystem.feature.calendar.viewmodel.WorkoutResultUiModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth = remember { YearMonth.now() }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    
    val selectedDate = uiState.selectedDate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(16.dp)
    ) {
        Text(
            text = "КОНТРОЛЬ ЦИКЛУ",
            color = NeonCyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("uk")).uppercase(),
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
            items(weekDays) { day ->
                Text(
                    text = day,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(firstDayOfWeek - 1) { Box(Modifier.size(40.dp)) }

            items(daysInMonth) { dayIndex ->
                val date = currentMonth.atDay(dayIndex + 1)
                val cycleDay = viewModel.getCycleDay(date)
                
                CalendarDayCell(
                    date = date,
                    cycleDay = cycleDay,
                    isToday = date == LocalDate.now(),
                    isSelected = date == selectedDate,
                    onClick = { viewModel.onDateSelected(date) }
                )
            }
        }

        if (selectedDate != null) {
            Spacer(Modifier.height(24.dp))
            DailyScheduleSection(
                date = selectedDate,
                cycleDay = viewModel.getCycleDay(selectedDate),
                results = uiState.workoutResults,
                onDismiss = { viewModel.onDateSelected(null) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate,
    cycleDay: Int,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val indicatorColor = when(cycleDay) {
        1 -> NeonRed
        2 -> NeonGold
        3 -> NeonCyan
        else -> NeonGreen
    }

    val borderColor = if (isSelected) NeonCyan else if (isToday) NeonCyanDim else Color.Transparent

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(if (isSelected) NeonCyan.copy(0.1f) else Color.Transparent)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isToday) NeonCyan else TextPrimary,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(6.dp)
                .background(indicatorColor, CircleShape)
        )
    }
}

@Composable
fun DailyScheduleSection(
    date: LocalDate,
    cycleDay: Int,
    results: List<WorkoutResultUiModel>,
    onDismiss: () -> Unit,
    viewModel: CalendarViewModel
) {
    val schedule by viewModel.getScheduleForDay(cycleDay).collectAsState(null)

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
            Text(
                text = "ДЕНЬ ${date.dayOfMonth}.${date.monthValue}",
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Text("[X]", color = NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        
        Text(
            text = when(cycleDay) {
                1 -> "СТАТУС: Денна зміна"
                2 -> "СТАТУС: Нічна зміна"
                3 -> "СТАТУС: Тренування А"
                else -> "СТАТУС: Тренування Б"
            },
            color = if (cycleDay <= 2) NeonRed else NeonGreen,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        HorizontalDivider(color = NeonCyan.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        if (results.isNotEmpty()) {
            Text("РЕЗУЛЬТАТИ ТРЕНУВАННЯ:", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            results.forEach { result ->
                Text(result.exerciseName.uppercase(), color = NeonGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                val setsText = result.sets.joinToString(" | ") { "${it.weight}кг x ${it.reps}" }
                Text(setsText, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
            }
            HorizontalDivider(color = NeonCyan.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        }

        schedule?.let { data ->
            if (data.workoutTemplateName != null) {
                Text(
                    text = "ПЛАНОВА ПРОГРАМА: ${data.workoutTemplateName}",
                    color = NeonGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                data.exerciseNames.forEach { name ->
                    Text(
                        text = "• $name",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "РЕЖИМ: АКТИВНЕ ВІДНОВЛЕННЯ",
                    color = NeonCyanDim,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
