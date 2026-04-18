package com.ihor.thesystem.feature.statistics.ui

import android.content.pm.ActivityInfo
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020408))
    ) {
        // Shared dynamic background for consistency
        AnimatedAnnualBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "РІЧНА МАТРИЦЯ ПРОГРЕСІЇ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )
                }
                
                Surface(
                    color = NeonGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "RANK-S PROJECTION",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
            }

            // --- Table Wrapper ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.02f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    MatrixHeaderRow()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(matrixData) { row ->
                            MatrixDataRow(row)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedAnnualBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF020408))
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonGreen.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.1f + (size.width * 0.05f * colorShift), size.height * 0.2f),
                radius = 400.dp.toPx()
            )
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonGold.copy(alpha = 0.03f), Color.Transparent),
                center = Offset(size.width * 0.9f - (size.width * 0.05f * colorShift), size.height * 0.8f),
                radius = 500.dp.toPx()
            )
        )
    }
}

@Composable
private fun MatrixHeaderRow() {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.03f))
    ) {
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(56.dp)
                .padding(start = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Вправа",
                style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
            )
        }
        for (m in 0..12) {
            val rankInfo = getRankInfo(m)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "M$m",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f))
                    )
                    Text(
                        text = rankInfo.first,
                        style = MaterialTheme.typography.labelMedium.copy(color = rankInfo.second, fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixDataRow(row: MatrixRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(52.dp)
                .padding(start = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = row.exercise.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1
            )
        }
        row.targets.forEachIndexed { index, target ->
            val rankInfo = getRankInfo(index)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = target,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = rankInfo.second.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
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
