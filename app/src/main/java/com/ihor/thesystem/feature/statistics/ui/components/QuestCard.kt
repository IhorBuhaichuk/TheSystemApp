package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.domain.model.DomainQuestType

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun QuestCard(
    quest: QuestUiModel,
    type: DomainQuestType,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPromotion = type == DomainQuestType.PROMOTION
    val accentColor = when (type) {
        DomainQuestType.DAILY -> NeonCyan
        DomainQuestType.MAIN -> NeonGold
        DomainQuestType.PROMOTION -> Color(0xFFFF003C) // Агресивний червоний для екзамену
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(accentColor.copy(alpha = 0.6f), PanelSurface, 12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isPromotion) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ЕКЗАМЕН",
                    color = accentColor,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    color = if (isPromotion) accentColor else TextPrimary,
                    fontFamily = if (isPromotion) TekoFamily else RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isPromotion) 22.sp else 16.sp
                )
                Text(
                    text = quest.subtitle,
                    color = TextSecondary,
                    fontFamily = RajdhaniFamily,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = if (isPromotion) Icons.Filled.Star else Icons.Filled.Assignment,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Відображення прогресу завдань
        if (quest.tasks.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            quest.tasks.forEach { task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (task.isCompleted) accentColor else Color.Gray.copy(alpha = 0.4f))
                    )
                    Text(
                        text = task.name,
                        color = if (task.isCompleted) TextPrimary else TextSecondary.copy(alpha = 0.6f),
                        fontFamily = RajdhaniFamily,
                        fontSize = 12.sp,
                        style = if (task.isCompleted) androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        ) else androidx.compose.ui.text.TextStyle.Default
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyQuestCard(type: DomainQuestType) {
    val label = when(type) {
        DomainQuestType.DAILY -> "РУТИНА ВІДСУТНЯ"
        DomainQuestType.MAIN -> "ОСНОВНИЙ КВЕСТ ВІДСУТНІЙ"
        DomainQuestType.PROMOTION -> "ЕКЗАМЕНИ ВІДСУТНІ"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .sciPanel(PanelBorder.copy(alpha = 0.2f), PanelSurface.copy(alpha = 0.5f), 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = TextSecondary.copy(alpha = 0.4f),
            fontFamily = RajdhaniFamily,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}
