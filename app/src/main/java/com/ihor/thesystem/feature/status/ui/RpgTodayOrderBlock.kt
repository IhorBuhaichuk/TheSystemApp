package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel

@Composable
internal fun TodayOrderBlock(
    decision: TodayTrainingDecision?,
    fallbackMainQuest: QuestUiModel?,
    onStartWorkout: () -> Unit
) {
    val colors = SystemTheme.colors
    val decisionType = decision?.decisionType
    val accent = decision.orderAccent()
    val readinessProgress = ((decision?.readinessScore ?: 0) / 100f).coerceIn(0f, 1f)
    val actionEnabled = decisionType != null && decisionType != TodayTrainingDecisionType.REST
    val actionText = when (decisionType) {
        TodayTrainingDecisionType.NO_EXCUSE -> "Почати 7 хв"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Почати відновлення"
        TodayTrainingDecisionType.DELOAD -> "Почати deload"
        TodayTrainingDecisionType.REST -> "День без тренування"
        null -> "План формується"
        else -> "Почати тренування"
    }

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true, contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Сьогоднішній наказ",
                subtitle = "Поточна дія дня"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusIcon(
                    icon = Icons.Filled.FitnessCenter,
                    tint = accent
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "Сьогодні: ${decision.todayTitle(fallbackMainQuest)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Рішення системи: ${decision.shortDecisionLabel()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = accent,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SystemStateBadge(
                        text = "Готовність: ${decision?.readinessScore ?: 0}%",
                        accent = accent
                    )
                    SystemStateBadge(
                        text = decision.shortDecisionLabel(),
                        accent = accent
                    )
                }
                SystemProgressBar(
                    progress = readinessProgress,
                    accent = accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            }

            Text(
                text = decision.displayReason(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            SystemButton(
                text = actionText,
                icon = Icons.Filled.FitnessCenter,
                onClick = onStartWorkout,
                enabled = actionEnabled,
                accent = accent,
                glow = actionEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TodayTrainingDecision?.orderAccent(): Color {
    val colors = SystemTheme.colors
    return when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> colors.accentSuccess
        TodayTrainingDecisionType.STANDARD_TRAINING,
        TodayTrainingDecisionType.NO_EXCUSE -> colors.accentPrimary
        TodayTrainingDecisionType.REDUCED_LOAD -> colors.accentWarning
        TodayTrainingDecisionType.ACTIVE_RECOVERY,
        TodayTrainingDecisionType.DELOAD -> colors.accentAi
        TodayTrainingDecisionType.REST -> colors.textMuted
        null -> colors.textMuted
    }
}

private fun TodayTrainingDecision?.todayTitle(fallbackMainQuest: QuestUiModel?): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.NO_EXCUSE -> "No Excuse Protocol"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Recovery Protocol"
        TodayTrainingDecisionType.DELOAD -> "Deload Session"
        TodayTrainingDecisionType.REST -> "День без тренування"
        null -> fallbackMainQuest?.title ?: "Recovery Protocol"
        else -> workoutName ?: fallbackMainQuest?.title ?: "Recovery Protocol"
    }

private fun TodayTrainingDecision?.shortDecisionLabel(): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> "Прогрес дозволено"
        TodayTrainingDecisionType.STANDARD_TRAINING -> "Планове тренування"
        TodayTrainingDecisionType.REDUCED_LOAD -> "Зменшити навантаження"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Відновлення"
        TodayTrainingDecisionType.NO_EXCUSE -> "План перераховано"
        TodayTrainingDecisionType.DELOAD -> "Делоад"
        TodayTrainingDecisionType.REST -> "Відпочинок"
        null -> "План синхронізується"
    }

private fun TodayTrainingDecision?.displayReason(): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED ->
            "Готовність висока, борг відновлення низький."
        TodayTrainingDecisionType.STANDARD_TRAINING ->
            "План на сьогодні підходить під поточний стан."
        TodayTrainingDecisionType.REDUCED_LOAD ->
            "Сьогодні краще знизити вагу та обсяг."
        TodayTrainingDecisionType.ACTIVE_RECOVERY ->
            "Організму потрібне відновлення замість силового навантаження."
        TodayTrainingDecisionType.NO_EXCUSE ->
            if (reason.contains("missed", ignoreCase = true)) {
                "Система зафіксувала пропуск. План перераховано. Наступна оптимальна дія: коротке тренування."
            } else {
                "Готовність нижча за планову. Наступна оптимальна дія: коротке тренування."
            }
        TodayTrainingDecisionType.DELOAD ->
            "Навантаження накопичилось, сьогодні працюємо легше."
        TodayTrainingDecisionType.REST ->
            "Сьогодні день без тренування."
        null ->
            "Система готує сьогоднішній план."
    }
