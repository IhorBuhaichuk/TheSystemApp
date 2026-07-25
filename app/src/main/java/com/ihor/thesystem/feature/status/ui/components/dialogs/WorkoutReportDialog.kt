package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.asString
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogScaffold
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemPlateShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.domain.model.AiArchitectReport
import com.ihor.thesystem.domain.model.SystemWorkoutGrade
import com.ihor.thesystem.domain.model.SystemWorkoutJudgment
import com.ihor.thesystem.domain.model.WorkoutPerformanceStatus
import com.ihor.thesystem.domain.model.WorkoutProgressionDecision

@Composable
fun WorkoutReportDialog(
    report: AiArchitectReport,
    onDismiss: () -> Unit,
    onOpenAnalysis: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = SystemTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SystemDialogScaffold(
            title = "Звіт тренування",
            onDismiss = onDismiss,
            accent = colors.accentAi,
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemGhostButton(
                        text = "Відкрити аналіз",
                        onClick = onOpenAnalysis,
                        modifier = Modifier.fillMaxWidth(),
                        accent = colors.accentAi
                    )
                    SystemButton(
                        text = "Прийняти",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        accent = colors.accentPrimary,
                        glow = true
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SystemScreenPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkoutCompletionBlock(report = report)

                report.judgment?.let { judgment ->
                    SystemJudgmentBlock(judgment = judgment)
                }

                Text(
                    text = report.architectFeedback.asString(context),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.textPrimary.copy(alpha = 0.92f)
                    )
                )

                HorizontalDivider(color = colors.borderSubtle)

                Text(
                    text = "Вікно відновлення: ${report.recoveryWindowHours.toInt()} годин",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                NextWorkoutBlock(report = report)

                if (report.isFallback) {
                    Text(
                        text = "ШІ тимчасово недоступний. Тренування збережено, а підсумок сформовано на пристрої.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WorkoutCompletionBlock(
    report: AiArchitectReport,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val judgment = report.judgment
    val progressAccent = judgment?.progressionDecision?.progressColor() ?: colors.accentPrimary
    val completedText = "${report.completedExercises.size} вправ · ${judgment?.completionPercent ?: 0}% плану"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .reportPlate(progressAccent),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = colors.accentSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Виконано",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                )
            }
            SystemStatusChip(
                text = judgment?.progressionDecision?.progressLabel() ?: "Записано",
                accent = progressAccent,
                active = true
            )
        }

        Text(
            text = completedText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SystemJudgmentBlock(
    judgment: SystemWorkoutJudgment,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val accent = judgment.progressionDecision.progressColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .techSurface(
                shape = systemPlateShape(),
                active = true,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Оцінка тренування: ${judgment.displayGrade()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "${judgment.completionPercent}%",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = judgment.grade.gradeColor(),
                    fontWeight = FontWeight.Black
                )
            )
        }

        Text(
            text = "Рішення системи: ${judgment.progressionDecision.label()}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.accentPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = judgment.reason,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
        )

        Text(
            text = judgment.nextAction,
            style = MaterialTheme.typography.labelMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun NextWorkoutBlock(
    report: AiArchitectReport,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val judgment = report.judgment
    val targetText = when {
        report.nextWorkoutDirectives.isNotEmpty() ->
            "Оновлено цілі для ${report.nextWorkoutDirectives.size} вправ. ${judgment?.nextAction.orEmpty()}"
        judgment != null ->
            judgment.nextAction
        else ->
            "Система збереже результат і врахує його в наступному плані на сьогодні."
    }.ifBlank {
        "Система збереже результат і врахує його в наступному плані на сьогодні."
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .reportPlate(colors.accentPrimary),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colors.accentPrimary,
            modifier = Modifier.size(18.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Наступного разу",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = targetText,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Modifier.reportPlate(accent: Color): Modifier {
    return this
        .techSurface(
            shape = systemPlateShape(),
            active = false,
            accent = accent,
            role = TechSurfaceRole.Plate
        )
        .padding(14.dp)
}

@Composable
private fun SystemWorkoutGrade.gradeColor() =
    when (this) {
        SystemWorkoutGrade.S,
        SystemWorkoutGrade.A -> SystemTheme.colors.accentSuccess
        SystemWorkoutGrade.B -> SystemTheme.colors.accentPrimary
        SystemWorkoutGrade.C -> SystemTheme.colors.accentWarning
        SystemWorkoutGrade.D -> SystemTheme.colors.accentAi
    }

private fun SystemWorkoutJudgment.displayGrade(): String =
    if (grade == SystemWorkoutGrade.A && performanceStatus == WorkoutPerformanceStatus.COMPLETED_HARD) {
        "A-"
    } else {
        grade.name
    }

private fun WorkoutProgressionDecision.label(): String =
    when (this) {
        WorkoutProgressionDecision.INCREASE_ALLOWED -> "можна підвищувати"
        WorkoutProgressionDecision.HOLD -> "вага не підвищується"
        WorkoutProgressionDecision.REDUCE -> "знизити навантаження"
        WorkoutProgressionDecision.DELOAD_RECOMMENDED -> "рекомендовано легше тренування"
    }

@Composable
private fun WorkoutProgressionDecision.progressColor(): Color =
    when (this) {
        WorkoutProgressionDecision.INCREASE_ALLOWED -> SystemTheme.colors.accentSuccess
        WorkoutProgressionDecision.HOLD -> SystemTheme.colors.accentPrimary
        WorkoutProgressionDecision.REDUCE,
        WorkoutProgressionDecision.DELOAD_RECOMMENDED -> SystemTheme.colors.accentWarning
    }

private fun WorkoutProgressionDecision.progressLabel(): String =
    when (this) {
        WorkoutProgressionDecision.INCREASE_ALLOWED -> "Підвищення"
        WorkoutProgressionDecision.HOLD -> "Без змін"
        WorkoutProgressionDecision.REDUCE -> "Зменшення"
        WorkoutProgressionDecision.DELOAD_RECOMMENDED -> "Розвантаження"
    }
