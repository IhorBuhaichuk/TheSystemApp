package com.ihor.thesystem.feature.statistics.ui

import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.util.LockScreenOrientation
import com.ihor.thesystem.domain.model.AnnualMatrixProvider
import com.ihor.thesystem.domain.model.MatrixRow

@Composable
fun AnnualProgressionScreen(navController: NavHostController) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    val matrixData = AnnualMatrixProvider.getMatrix()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "РІЧНА МАТРИЦЯ ПРОГРЕСІЇ",
                color = NeonGreen,
                fontFamily = TekoFamily,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = NeonRed)
            }
        }

        // --- Table Wrapper ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. СТАТИЧНА ШАПКА (Header Row) - завжди видима зверху
            MatrixHeaderRow()

            // 2. ВЕРТИКАЛЬНИЙ СПИСОК (Дані)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(matrixData) { row ->
                    MatrixDataRow(row)
                }
            }
        }
    }
}

@Composable
private fun MatrixHeaderRow() {
    Row(
        modifier = Modifier
            .background(PanelSurface)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        // Заголовок для назв вправ
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(44.dp)
                .border(0.5.dp, Color.White.copy(alpha = 0.2f))
                .padding(start = 8.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Вправа",
                color = TextSecondary,
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        // Заголовки для місяців (M0 - M12)
        for (m in 0..12) {
            val rankInfo = getRankInfo(m)
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(44.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M$m (${rankInfo.first})",
                    color = rankInfo.second,
                    fontFamily = TekoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MatrixDataRow(row: MatrixRow) {
    Row(
        modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        // Назва вправи
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(40.dp)
                .border(0.5.dp, Color.White.copy(alpha = 0.1f))
                .padding(start = 8.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = row.exercise.uppercase(),
                color = TextPrimary,
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.Start
            )
        }
        // Значення для кожного місяця
        row.targets.forEachIndexed { index, target ->
            val rankInfo = getRankInfo(index)
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(40.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = target,
                    color = rankInfo.second.copy(alpha = 0.9f),
                    fontFamily = TekoFamily,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getRankInfo(month: Int): Pair<String, Color> {
    return when (month) {
        0, 1 -> "E" to Color.DarkGray
        2, 3 -> "D" to Color(0xFF1E90FF)
        4, 5 -> "C" to Color(0xFF00FF00)
        6, 7 -> "B" to Color(0xFFFFD700)
        8, 9 -> "A" to Color(0xFFA020F0)
        10, 11, 12 -> "S" to Color(0xFFFF003C)
        else -> "" to Color.White
    }
}
