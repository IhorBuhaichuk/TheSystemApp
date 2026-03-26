package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.*
import com.ihor.thesystem.feature.status.viewmodel.DebuffUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerLeftPanel(
    data: StatusUiData,
    modifier: Modifier = Modifier,
    onNameTap: () -> Unit = {},
    onDebuffEdit: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .sciPanel(
                borderColor     = PanelBorder,
                backgroundColor = PanelSurface.copy(alpha = 0.92f),
                cornerCut       = 14.dp
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── "Player Status" label ──────────────────────────────────────
        Text(
            text       = "Player Status",
            color      = TextSecondary,
            fontSize   = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        // ── Player Name — glitch ──────────────────────────────────────
        GlitchText(
            text     = data.playerName,
            style    = TextStyle(
                fontSize   = 34.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onNameTap, onLongClick = onNameTap)
        )

        // ── Class ─────────────────────────────────────────────────────
        Text(
            text       = "[ КЛАС: ${data.playerClass} ]",
            color      = TextSecondary,
            fontSize   = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        // ── Progress bar ──────────────────────────────────────────────
        WorkoutProgressBar(
            completed  = data.monthWorkoutsCompleted,
            total      = data.monthWorkoutsTotal,
            modifier   = Modifier
                .fillMaxWidth()
                .height(18.dp)
        )

        // ── Debuff block ──────────────────────────────────────────────
        DebuffBlock(
            debuffs  = data.activeDebuffs,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onDebuffEdit, onLongClick = onDebuffEdit)
        )

        // ── Cycle counter ─────────────────────────────────────────────
        CycleCounter(currentDay = data.cycleDay)
    }
}

// ─── Private sub-composables ─────────────────────────────────────────────────

@Composable
private fun WorkoutProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    Canvas(modifier = modifier) {
        val cr = CornerRadius(4.dp.toPx())
        // Track
        drawRoundRect(color = BackgroundDeep, cornerRadius = cr)
        // Fill
        if (progress > 0f) {
            drawRoundRect(
                color        = NeonCyan,
                size         = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = cr
            )
        }
        // Tick marks
        if (total > 1) {
            for (i in 1 until total) {
                val x = size.width / total * i
                drawLine(
                    color       = BackgroundDeep.copy(alpha = 0.8f),
                    start       = Offset(x, 0f),
                    end         = Offset(x, size.height),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
        // Border
        drawRoundRect(
            color        = PanelBorder,
            cornerRadius = cr,
            style        = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
private fun DebuffBlock(
    debuffs: List<DebuffUiModel>,
    modifier: Modifier = Modifier
) {
    val hasPenalty = debuffs.any { it.penaltyPercent > 0 && it.isActive }
    val borderCol  = if (hasPenalty) NeonRed else NeonRed.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .sciPanel(
                borderColor     = borderCol,
                backgroundColor = DangerRed.copy(alpha = 0.18f),
                cornerCut       = 6.dp
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (debuffs.isEmpty()) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Debuff warning",
                tint   = NeonRed,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                debuffs.forEach { d ->
                    Text(
                        text       = "▲ ${d.text}",
                        color      = NeonRed,
                        fontSize   = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleCounter(
    currentDay: Int,
    totalDays: Int = 4
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text          = "CYCLE COUNTER",
            color         = NeonCyanDim,
            fontSize      = 9.sp,
            fontFamily    = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (day in 1..totalDays) {
                CycleHex(isActive = day <= currentDay)
            }
        }
    }
}

@Composable
private fun CycleHex(isActive: Boolean) {
    Canvas(modifier = Modifier.size(30.dp)) {
        val path = buildHexagonPath(size, rotationDegrees = 30f)
        if (isActive) {
            drawPath(path, NeonCyan)
        } else {
            drawPath(path, NeonCyan.copy(alpha = 0.12f))
            drawPath(path, NeonCyan, style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}