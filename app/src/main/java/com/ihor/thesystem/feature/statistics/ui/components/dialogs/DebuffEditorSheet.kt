package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.domain.model.DebuffConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebuffEditorSheet(
    debuffs: List<DebuffConfig>,
    onToggle: (DebuffConfig) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .sciPanel(
                        borderColor     = NeonRed.copy(alpha = 0.3f),
                        backgroundColor = NeonRed.copy(alpha = 0.2f),
                        cornerCut       = 2.dp
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text          = "[ РЕДАКТОР ДЕБАФІВ ]",
                color         = NeonRed,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                fontSize      = 14.sp,
                letterSpacing = 1.sp
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(debuffs) { debuff ->
                    DebuffToggleRow(debuff = debuff, onToggle = { onToggle(debuff) })
                }
            }
        }
    }
}

@Composable
private fun DebuffToggleRow(
    debuff: DebuffConfig,
    onToggle: () -> Unit
) {
    val borderColor = if (debuff.isActive) NeonRed else NeonRed.copy(alpha = 0.3f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor     = borderColor,
                backgroundColor = if (debuff.isActive) NeonRed.copy(0.12f) else PanelSurface,
                cornerCut       = 8.dp
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = debuff.condition,
                color      = if (debuff.isActive) NeonRed else TextSecondary,
                fontSize   = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text       = debuff.text,
                color      = TextSecondary,
                fontSize   = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            if (debuff.penaltyPercent > 0) {
                Text(
                    text       = "Штраф: -${debuff.penaltyPercent}%",
                    color      = NeonRed,
                    fontSize   = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Switch(
            checked         = debuff.isActive,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = NeonRed,
                checkedTrackColor  = NeonRed.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = PanelSurface
            )
        )
    }
}