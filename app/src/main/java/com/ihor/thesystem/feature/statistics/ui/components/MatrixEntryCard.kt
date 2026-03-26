package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatrixEntryCard(
    entry: MatrixEntryUiModel,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    // Колір по прогресу
    val accentColor: Color = when {
        entry.progressPercent >= 1f  -> NeonGreen
        entry.progressPercent >= 0.5f -> NeonCyan
        else                          -> NeonCyanDim
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(accentColor.copy(0.35f), PanelSurface, 8.dp)
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Top row: name + edit hint ─────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = entry.exerciseName,
                color      = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                modifier   = Modifier.weight(1f)
            )
            Icon(
                imageVector        = Icons.Filled.Edit,
                contentDescription = "Long press to edit",
                tint               = TextSecondary.copy(alpha = 0.4f),
                modifier           = Modifier.size(13.dp)
            )
        }

        // ── Progress bar ──────────────────────────────────────────────
        MatrixProgressBar(
            progress    = entry.progressPercent,
            accentColor = accentColor
        )

        // ── Weight row ────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeightLabel("СТАРТ",    entry.displayStart,   TextSecondary)
            WeightLabel("ЗАРАЗ",    entry.displayCurrent, accentColor)
            WeightLabel("ЦІЛЬ",     entry.displayTarget,  NeonGold)
        }

        // ── Weekly step ───────────────────────────────────────────────
        if (entry.weeklyStep > 0f) {
            Text(
                text       = "+${String.format("%.2f", entry.weeklyStep)}кг / тиждень",
                color      = TextSecondary.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize   = 9.sp
            )
        }
    }
}

@Composable
private fun WeightLabel(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = label,
            color      = TextSecondary.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            fontSize   = 8.sp
        )
        Text(
            text       = value,
            color      = valueColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize   = 12.sp
        )
    }
}