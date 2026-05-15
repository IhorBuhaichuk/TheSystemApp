package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.RajdhaniFamily
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.theme.TekoFamily
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.core.ui.components.glassCard
import com.ihor.thesystem.core.ui.components.neonGlow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.domain.model.DomainQuestType

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun QuestCard(
    quest: QuestUiModel,
    type: DomainQuestType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val colors = SystemTheme.colors
    val isPromotion = type == DomainQuestType.PROMOTION
    val shapes = SystemTheme.shapes
    val accentColor = when (type) {
        DomainQuestType.DAILY -> colors.accentPrimary
        DomainQuestType.MAIN -> colors.accentWarning
        DomainQuestType.PROMOTION -> colors.accentError
    }

    // ФІКС: Картка завжди активна і має світіння, якщо вона не завершена
    val isInteractive = !quest.isCompleted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!quest.isCompleted) Modifier.neonGlow(accentColor, radius = if (isPromotion) 12.dp else 8.dp)
                else Modifier
            )
            .glassCard()
            .then(
                if (isPromotion) Modifier.border(2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(shapes.medium))
                else Modifier
            )
            .combinedClickable(
                enabled = true, // Завжди клікабельна для перегляду деталей
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(SystemCardPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isPromotion) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(shapes.extraSmall))
                    .background(accentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.text_exam),
                    color = accentColor,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.sp
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
                    color = if (quest.isCompleted) colors.textSecondary else (if (isPromotion) accentColor else colors.textPrimary),
                    fontFamily = if (isPromotion) TekoFamily else RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isPromotion) 28.sp else 16.sp,
                    lineHeight = if (isPromotion) 32.sp else 20.sp
                )
                Text(
                    text = quest.subtitle.asString(),
                    color = if (quest.isCompleted) colors.textSecondary.copy(alpha = 0.5f) else (if (isPromotion) accentColor.copy(alpha = 0.8f) else colors.textSecondary),
                    fontFamily = RajdhaniFamily,
                    fontSize = 11.sp,
                    fontWeight = if (isPromotion) FontWeight.Bold else FontWeight.Normal
                )
            }
            Icon(
                imageVector = if (isPromotion) Icons.Filled.Star else Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                tint = if (quest.isCompleted) colors.textMuted.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(if (isPromotion) 32.dp else 24.dp)
                    .then(
                        if (!quest.isCompleted && isPromotion) Modifier.neonGlow(accentColor, 12.dp)
                        else Modifier
                    )
            )
        }

        // Відображення прогресу завдань
        if (quest.tasks.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            val visibleTasks = quest.tasks.take(3)
            val remainingCount = quest.tasks.size - 3

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleTasks.forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (task.isCompleted) accentColor else colors.textMuted.copy(alpha = 0.4f))
                        )
                        Text(
                            text = task.name,
                            color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
                            fontFamily = RajdhaniFamily,
                            fontSize = 12.sp,
                            style = if (task.isCompleted) androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ) else androidx.compose.ui.text.TextStyle.Default
                        )
                    }
                }
                
                if (remainingCount > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.quest_tasks_more, remainingCount, remainingCount),
                        color = accentColor.copy(alpha = 0.7f),
                        fontFamily = RajdhaniFamily,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyQuestCard(type: DomainQuestType) {
    val colors = SystemTheme.colors
    val label = when(type) {
        DomainQuestType.DAILY -> stringResource(R.string.text_empty_routine)
        DomainQuestType.MAIN -> stringResource(R.string.text_empty_main_quest)
        DomainQuestType.PROMOTION -> stringResource(R.string.text_empty_promotions)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .sciPanel(colors.borderSubtle.copy(alpha = 0.2f), colors.surfaceGlassStrong.copy(alpha = 0.5f), SystemTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.textSecondary.copy(alpha = 0.4f),
            fontFamily = RajdhaniFamily,
            fontSize = 12.sp,
            letterSpacing = 0.sp
        )
    }
}
