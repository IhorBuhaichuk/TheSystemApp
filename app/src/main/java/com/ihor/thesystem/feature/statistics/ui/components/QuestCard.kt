package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.core.ui.components.glassCard
import com.ihor.thesystem.core.ui.components.neonGlow
import com.ihor.thesystem.core.ui.components.systemCombinedClickable
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
        DomainQuestType.PROMOTION -> colors.accentWarning
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
            .systemCombinedClickable(
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
                    text = "Контрольний норматив",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Black
                    )
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (quest.isCompleted) colors.textSecondary else if (isPromotion) accentColor else colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = quest.subtitle.asString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (quest.isCompleted) colors.textSecondary.copy(alpha = 0.5f) else if (isPromotion) accentColor.copy(alpha = 0.8f) else colors.textSecondary,
                        fontWeight = if (isPromotion) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                tint = if (quest.isCompleted) colors.textMuted.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(if (isPromotion) 28.dp else 24.dp)
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
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (remainingCount > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.quest_tasks_more, remainingCount, remainingCount),
                        modifier = Modifier.padding(start = 14.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
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
            .sciPanel(colors.borderSubtle, colors.surfaceGlassSoft, SystemTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary.copy(alpha = 0.4f)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
