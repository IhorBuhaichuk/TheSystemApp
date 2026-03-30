package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

@Composable
fun MatrixEntryCard(
    entry: MatrixEntryUiModel,
    onCardClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
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
            .alpha(if (entry.isActive) 1f else 0.4f)
            .sciPanel(accentColor.copy(0.35f), PanelSurface, 8.dp)
            .clickable(enabled = entry.isActive) { onCardClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Top row: name + setup button ──────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = entry.exerciseName.uppercase(),
                color      = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                modifier   = Modifier.weight(1f)
            )
            IconButton(
                onClick = onSetupClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = "Setup Goals",
                    tint = NeonGold,
                    modifier = Modifier.size(18.dp)
                )
            }
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
            WeightBlock("СТАРТ",    entry.displayStart,   TextSecondary)
            WeightBlock("ЗАРАЗ",    entry.displayCurrent, accentColor)
            WeightBlock("ЦІЛЬ",     entry.displayTarget,  NeonGold)
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
private fun WeightBlock(label: String, value: String, valueColor: Color) {
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
