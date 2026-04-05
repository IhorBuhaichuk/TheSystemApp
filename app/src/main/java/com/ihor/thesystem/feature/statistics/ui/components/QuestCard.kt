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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // ФІКС: Картка завжди активна і має світіння, якщо вона не завершена
    val isInteractive = !quest.isCompleted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor = if (isPromotion) accentColor else accentColor.copy(alpha = 0.8f),
                backgroundColor = PanelSurface,
                cornerCut = 12.dp,
                borderWidth = if (isPromotion) 2.dp else 1.dp
            )
            .combinedClickable(
                enabled = true, // Завжди клікабельна для перегляду деталей
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
                    color = if (quest.isCompleted) TextSecondary else (if (isPromotion) accentColor else TextPrimary),
                    fontFamily = if (isPromotion) TekoFamily else RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isPromotion) 28.sp else 16.sp,
                    lineHeight = if (isPromotion) 32.sp else 20.sp
                )
                Text(
                    text = quest.subtitle,
                    color = if (quest.isCompleted) TextSecondary.copy(alpha = 0.5f) else (if (isPromotion) accentColor.copy(alpha = 0.8f) else TextSecondary),
                    fontFamily = RajdhaniFamily,
                    fontSize = 11.sp,
                    fontWeight = if (isPromotion) FontWeight.Bold else FontWeight.Normal
                )
            }
            Icon(
                imageVector = if (isPromotion) Icons.Filled.Star else Icons.Filled.Assignment,
                contentDescription = null,
                tint = if (quest.isCompleted) Color.Gray.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(if (isPromotion) 32.dp else 24.dp)
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
                        color = if (task.isCompleted) TextSecondary else TextPrimary,
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
