package com.ihor.thesystem.feature.architect.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.UkrainianLocale
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemMetricCard
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.domain.model.AnnualProgressComparison
import com.ihor.thesystem.domain.model.AnnualProgressStatus
import com.ihor.thesystem.domain.model.ExerciseProgressAnalysis
import com.ihor.thesystem.domain.model.ExerciseProgressStatus
import com.ihor.thesystem.domain.model.MotivationLevelResult
import com.ihor.thesystem.domain.model.NextWorkoutRecommendationAnalysis
import com.ihor.thesystem.domain.model.WorkoutAnalysisData
import com.ihor.thesystem.domain.model.WorkoutExecutionAnalysis
import com.ihor.thesystem.feature.architect.viewmodel.WorkoutAnalysisUiTextMapper
import com.ihor.thesystem.feature.architect.viewmodel.WorkoutAnalysisViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WorkoutAnalysisScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        when (val state = uiState) {
            UiState.Loading -> WorkoutAnalysisLoading()
            is UiState.Error -> WorkoutAnalysisMessage(
                title = "Аналіз недоступний",
                message = state.message.asString(),
                onBack = onBack,
                onRetry = viewModel::loadAnalysis
            )
            is UiState.Content -> if (state.data == null) {
                WorkoutAnalysisMessage(
                    title = "Немає завершеного тренування",
                    message = "Система сформує аналіз після першого збереженого тренування.",
                    onBack = onBack,
                    onRetry = viewModel::loadAnalysis
                )
            } else {
                WorkoutAnalysisContent(
                    analysis = state.data,
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun WorkoutAnalysisContent(
    analysis: WorkoutAnalysisData,
    onBack: () -> Unit
) {
    var showMotivationInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        WorkoutAnalysisHeader(
            analysis = analysis,
            onBack = onBack
        )
        ExecutionBlock(execution = analysis.execution)
        MotivationLevelBlock(
            result = analysis.motivationLevel,
            onInfoClick = { showMotivationInfo = true }
        )
        ExerciseProgressBlock(
            progress = analysis.exerciseProgress,
            isInitialDataCollection = analysis.isInitialDataCollection
        )
        AnnualProgressBlock(
            progress = analysis.annualProgress,
            isInitialDataCollection = analysis.isInitialDataCollection,
            adaptationRemainingDays = analysis.adaptationRemainingDays
        )
        RecommendationsBlock(recommendations = analysis.recommendations)
        analysis.aiFeedback?.takeIf { it.isNotBlank() }?.let { feedback ->
            SystemInsightBlock(text = feedback)
        }
    }

    if (showMotivationInfo) {
        MotivationInfoDialog(
            result = analysis.motivationLevel,
            onDismiss = { showMotivationInfo = false }
        )
    }
}

@Composable
private fun WorkoutAnalysisHeader(
    analysis: WorkoutAnalysisData,
    onBack: () -> Unit
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.overlayLight)
                .border(1.dp, colors.borderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = colors.textSecondary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = "Аналіз тренування",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(analysis.workoutName, analysis.sessionTimestamp.formatDate()).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExecutionBlock(execution: WorkoutExecutionAnalysis) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Виконання плану",
                subtitle = "Поточне або останнє завершене тренування"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemMetricCard(
                    label = "Підходи",
                    value = "${execution.completedSets}/${execution.plannedSets.coerceAtLeast(execution.completedSets)}",
                    accent = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricCard(
                    label = "Вправи",
                    value = execution.completedExercises.toString(),
                    accent = colors.accentSuccess,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricCard(
                    label = "Пропущено",
                    value = execution.skippedExercises.toString(),
                    accent = colors.accentWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MotivationLevelBlock(
    result: MotivationLevelResult,
    onInfoClick: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Загальна оцінка",
                subtitle = "Розраховано за вашими результатами",
                trailing = {
                    SmallInfoButton(onClick = onInfoClick)
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.accentAi,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                    )
                }
                Text(
                    text = "${result.finalScore}/100",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                )
            }
            SystemProgressBar(
                progress = result.finalScore / 100f,
                accent = colors.accentAi,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SmallInfoButton(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.accentAi.copy(alpha = 0.28f), CircleShape)
            .systemClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Як розраховується рівень",
            tint = colors.accentAi,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MotivationInfoDialog(
    result: MotivationLevelResult,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        DarkGlassCard(active = true, contentPadding = SystemCardPadding) {
            Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
                SystemSectionHeader(title = "Як розраховується рівень")
                Text(
                    text = WorkoutAnalysisUiTextMapper.motivationExplanation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutAnalysisUiTextMapper.motivationBreakdown(result).forEach { (label, score) ->
                        BreakdownRow(label = label, score = score)
                    }
                }
                SystemButton(
                    text = "Зрозуміло",
                    onClick = onDismiss,
                    accent = colors.accentAi,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, score: Int) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
        )
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun ExerciseProgressBlock(
    progress: List<ExerciseProgressAnalysis>,
    isInitialDataCollection: Boolean
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Силовий прогрес",
                subtitle = if (isInitialDataCollection) {
                    "Стартова база без критики"
                } else {
                    "Поточний результат проти попереднього"
                }
            )
            if (progress.isEmpty()) {
                EmptyAnalysisText(text = "У цьому тренуванні немає вагових підходів для розрахунку максимуму.")
            }
            progress.forEach { item ->
                val hideCriticism = isInitialDataCollection && item.status == ExerciseProgressStatus.Decreased
                val previousOneRepMax = item.previousEstimatedOneRepMax
                AnalysisRow(
                    title = item.exerciseName,
                    primary = "Розрахунковий максимум: ${item.currentEstimatedOneRepMax.formatWeight()} кг",
                    secondary = when {
                        hideCriticism ->
                            "Це корисна стартова точка для майбутнього графіка."
                        previousOneRepMax != null ->
                            "Попередній: ${previousOneRepMax.formatWeight()} кг · зміна ${item.difference.formatSignedWeight()}"
                        else ->
                            "Попереднього результату ще немає"
                    },
                    status = if (hideCriticism) {
                        "Збір бази"
                    } else {
                        WorkoutAnalysisUiTextMapper.exerciseStatusLabel(item.status)
                    },
                    accent = if (hideCriticism) colors.accentAi else item.status.statusAccent()
                )
            }
        }
    }
}

@Composable
private fun AnnualProgressBlock(
    progress: List<AnnualProgressComparison>,
    isInitialDataCollection: Boolean,
    adaptationRemainingDays: Long
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Порівняння з річним планом",
                subtitle = if (isInitialDataCollection) {
                    "Збір бази, ще ${adaptationRemainingDays} дн."
                } else {
                    "Результат проти збереженого плану"
                }
            )
            if (progress.isEmpty()) {
                EmptyAnalysisText(text = "У цьому тренуванні немає вагового результату для порівняння з річним планом.")
            }
            progress.forEach { item ->
                val isCollectingBase = isInitialDataCollection && item.status == AnnualProgressStatus.NoPlan
                val plannedWeight = item.plannedWeight
                AnalysisRow(
                    title = item.exerciseName,
                    primary = "Результат: ${item.factWeight.formatWeight()} кг",
                    secondary = when {
                        plannedWeight != null ->
                            "План: ${plannedWeight.formatWeight()} кг · зміна ${item.difference.formatSignedWeight()}"
                        isCollectingBase ->
                            "Перші 2 тижні система збирає стартові дані. Графік ще не оцінюється."
                        else ->
                            "Річний план ще можна сформувати на основі зібраної бази."
                    },
                    status = if (isCollectingBase) {
                        "Збір бази"
                    } else {
                        WorkoutAnalysisUiTextMapper.annualStatusLabel(item.status)
                    },
                    accent = if (isCollectingBase) colors.accentAi else item.status.statusAccent()
                )
            }
        }
    }
}

@Composable
private fun RecommendationsBlock(recommendations: List<NextWorkoutRecommendationAnalysis>) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Рекомендації на наступне",
                subtitle = "На основі ваших попередніх результатів"
            )
            if (recommendations.isEmpty()) {
                EmptyAnalysisText(text = "Вагових рекомендацій для цього тренування немає.")
            }
            recommendations.forEach { recommendation ->
                AnalysisRow(
                    title = recommendation.exerciseName,
                    primary = "${recommendation.recommendedWeight.formatWeight()} кг · ${recommendation.recommendedSets} × ${recommendation.recommendedReps}",
                    secondary = recommendation.reason,
                    status = "Наступний підхід",
                    accent = colors.accentAi
                )
            }
        }
    }
}

@Composable
private fun SystemInsightBlock(text: String) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Підсумок системи",
                subtitle = "Збережений висновок помічника"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.accentAi.copy(alpha = 0.075f))
                    .border(1.dp, colors.accentAi.copy(alpha = 0.18f), shape)
                    .padding(SystemItemSpacing),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = colors.accentAi,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyAnalysisText(text: String) {
    val colors = SystemTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            color = colors.textMuted,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun AnalysisRow(
    title: String,
    primary: String,
    secondary: String,
    status: String,
    accent: Color
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = primary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        SystemStatusChip(text = status, accent = accent, active = true)
    }
}

@Composable
private fun WorkoutAnalysisLoading() {
    val colors = SystemTheme.colors
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.accentAi)
    }
}

@Composable
private fun WorkoutAnalysisMessage(
    title: String,
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val colors = SystemTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.overlayLight)
                    .border(1.dp, colors.borderSubtle, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = colors.textSecondary)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        DarkGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                )
                SystemButton(
                    text = "Оновити",
                    icon = Icons.Filled.Refresh,
                    onClick = onRetry,
                    accent = colors.accentAi,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ExerciseProgressStatus.statusAccent(): Color {
    val colors = SystemTheme.colors
    return when (this) {
        ExerciseProgressStatus.Improved -> colors.accentSuccess
        ExerciseProgressStatus.Stable -> colors.accentPrimary
        ExerciseProgressStatus.Decreased -> colors.accentWarning
    }
}

@Composable
private fun AnnualProgressStatus.statusAccent(): Color {
    val colors = SystemTheme.colors
    return when (this) {
        AnnualProgressStatus.OnPlan -> colors.accentSuccess
        AnnualProgressStatus.BelowPlan -> colors.accentWarning
        AnnualProgressStatus.AbovePlan -> colors.accentPrimary
        AnnualProgressStatus.NoPlan -> colors.textMuted
    }
}

private fun Long.formatDate(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", UkrainianLocale))

private fun Double?.formatSignedWeight(): String =
    this?.let {
        val prefix = if (it > 0.0) "+" else ""
        "$prefix${it.formatWeight()} кг"
    } ?: "—"

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
