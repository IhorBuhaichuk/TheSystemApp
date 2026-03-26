package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Lens
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
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel

enum class QuestCardType { DAILY, MAIN }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestCard(
    quest: QuestUiModel,
    type: QuestCardType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val accentColor: Color = when {
        quest.isCompleted        -> NeonGreen
        type == QuestCardType.DAILY -> NeonCyan
        else                     -> NeonGold
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor     = accentColor,
                backgroundColor = PanelSurface.copy(alpha = 0.88f),
                cornerCut       = 10.dp
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(50.dp)
                .sciPanel(
                    borderColor     = accentColor.copy(alpha = 0.45f),
                    backgroundColor = accentColor.copy(alpha = 0.10f),
                    cornerCut       = 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                quest.isCompleted        -> Icons.Filled.CheckCircle
                type == QuestCardType.DAILY -> Icons.Filled.ChecklistRtl
                else                     -> Icons.Filled.Lens
            }
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(26.dp)
            )
        }

        // Text
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text          = quest.title,
                color         = accentColor,
                fontSize      = 13.sp,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text       = quest.subtitle,
                color      = TextSecondary,
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun EmptyQuestCard(
    type: QuestCardType,
    modifier: Modifier = Modifier
) {
    val accentColor = if (type == QuestCardType.DAILY) NeonCyanDim else NeonGold.copy(alpha = 0.5f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .sciPanel(
                borderColor     = accentColor,
                backgroundColor = PanelSurface.copy(alpha = 0.6f),
                cornerCut       = 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "[ КВЕСТІВ НЕ ЗНАЙДЕНО ]",
            color      = accentColor,
            fontSize   = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}