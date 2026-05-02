package com.ihor.thesystem.feature.architect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.ihor.thesystem.core.theme.AccentAi
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentSuccess
import com.ihor.thesystem.core.theme.AccentWarning
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemSurfaceGlass
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemMetricCard
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
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
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        RpgStatusBackdrop()

        when (val state = uiState) {
            UiState.Loading -> WorkoutAnalysisLoading()
            is UiState.Error -> WorkoutAnalysisMessage(
                title = "РђРЅР°Р»С–Р· РЅРµРґРѕСЃС‚СѓРїРЅРёР№",
                message = state.message.asString(),
                onBack = onBack,
                onRetry = viewModel::loadAnalysis
            )
            is UiState.Content -> if (state.data == null) {
                WorkoutAnalysisMessage(
                    title = "РќРµРјР°С” Р·Р°РІРµСЂС€РµРЅРѕРіРѕ С‚СЂРµРЅСѓРІР°РЅРЅСЏ",
                    message = "РЎРёСЃС‚РµРјР° СЃС„РѕСЂРјСѓС” Р°РЅР°Р»С–Р· РїС–СЃР»СЏ РїРµСЂС€РѕРіРѕ Р·Р±РµСЂРµР¶РµРЅРѕРіРѕ С‚СЂРµРЅСѓРІР°РЅРЅСЏ.",
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
            .padding(top = 16.dp, bottom = 24.dp),
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
        ExerciseProgressBlock(progress = analysis.exerciseProgress)
        AnnualProgressBlock(progress = analysis.annualProgress)
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
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, BorderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "РќР°Р·Р°Рґ",
                tint = TextSecondary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = "РђРЅР°Р»С–Р· С‚СЂРµРЅСѓРІР°РЅРЅСЏ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(analysis.workoutName, analysis.sessionTimestamp.formatDate()).joinToString(" В· "),
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExecutionBlock(execution: WorkoutExecutionAnalysis) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р’РёРєРѕРЅР°РЅРЅСЏ РїР»Р°РЅСѓ",
                subtitle = "РџРѕС‚РѕС‡РЅРµ Р°Р±Рѕ РѕСЃС‚Р°РЅРЅС” Р·Р°РІРµСЂС€РµРЅРµ С‚СЂРµРЅСѓРІР°РЅРЅСЏ"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemMetricCard(
                    label = "РџС–РґС…РѕРґРё",
                    value = "${execution.completedSets}/${execution.plannedSets.coerceAtLeast(execution.completedSets)}",
                    accent = AccentPrimary,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricCard(
                    label = "Р’РїСЂР°РІРё",
                    value = execution.completedExercises.toString(),
                    accent = AccentSuccess,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricCard(
                    label = "РџСЂРѕРїСѓС‰РµРЅРѕ",
                    value = execution.skippedExercises.toString(),
                    accent = AccentWarning,
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
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РњРѕС‚РёРІР°С†С–Р№РЅРёР№ СЂС–РІРµРЅСЊ",
                subtitle = "Domain score Р·Р° СЂРµР°Р»СЊРЅРёРјРё РјРµС‚СЂРёРєР°РјРё",
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
                            color = AccentAi,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                Text(
                    text = "${result.finalScore}/100",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                )
            }
            SystemProgressBar(
                progress = result.finalScore / 100f,
                accent = AccentAi,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SmallInfoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, AccentAi.copy(alpha = 0.28f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "РЇРє СЂРѕР·СЂР°С…РѕРІСѓС”С‚СЊСЃСЏ СЂС–РІРµРЅСЊ",
            tint = AccentAi,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MotivationInfoDialog(
    result: MotivationLevelResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DarkGlassCard(active = true, contentPadding = 18.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SystemSectionHeader(title = "РЇРє СЂРѕР·СЂР°С…РѕРІСѓС”С‚СЊСЃСЏ СЂС–РІРµРЅСЊ")
                Text(
                    text = WorkoutAnalysisUiTextMapper.motivationExplanation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutAnalysisUiTextMapper.motivationBreakdown(result).forEach { (label, score) ->
                        BreakdownRow(label = label, score = score)
                    }
                }
                SystemButton(
                    text = "Р—СЂРѕР·СѓРјС–Р»Рѕ",
                    onClick = onDismiss,
                    accent = AccentAi,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun ExerciseProgressBlock(progress: List<ExerciseProgressAnalysis>) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РџСЂРѕРіСЂРµСЃ",
                subtitle = "РџРѕС‚РѕС‡РЅРёР№ СЂРµР·СѓР»СЊС‚Р°С‚ РїСЂРѕС‚Рё РїРѕРїРµСЂРµРґРЅСЊРѕРіРѕ"
            )
            progress.forEach { item ->
                AnalysisRow(
                    title = item.exerciseName,
                    primary = "РџРѕС‚РѕС‡РЅРёР№ 1RM: ${item.currentEstimatedOneRepMax.formatWeight()} РєРі",
                    secondary = item.previousEstimatedOneRepMax?.let {
                        "РџРѕРїРµСЂРµРґРЅС–Р№: ${it.formatWeight()} РєРі В· О” ${item.difference.formatSignedWeight()}"
                    } ?: "РџРѕРїРµСЂРµРґРЅСЊРѕРіРѕ СЂРµР·СѓР»СЊС‚Р°С‚Сѓ С‰Рµ РЅРµРјР°С”",
                    status = WorkoutAnalysisUiTextMapper.exerciseStatusLabel(item.status),
                    accent = item.status.statusAccent()
                )
            }
        }
    }
}

@Composable
private fun AnnualProgressBlock(progress: List<AnnualProgressComparison>) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р’С–РґРЅРѕСЃРЅРѕ СЂС–С‡РЅРѕС— РїСЂРѕРіСЂРµСЃС–С—",
                subtitle = "Р¤Р°РєС‚ РїСЂРѕС‚Рё Р·Р±РµСЂРµР¶РµРЅРѕРіРѕ РїР»Р°РЅСѓ"
            )
            progress.forEach { item ->
                AnalysisRow(
                    title = item.exerciseName,
                    primary = "Р¤Р°РєС‚: ${item.factWeight.formatWeight()} РєРі",
                    secondary = item.plannedWeight?.let {
                        "РџР»Р°РЅ: ${it.formatWeight()} РєРі В· О” ${item.difference.formatSignedWeight()}"
                    } ?: "Р”Р»СЏ РІРїСЂР°РІРё С‰Рµ РЅРµРјР°С” Р·Р±РµСЂРµР¶РµРЅРѕРіРѕ СЂС–С‡РЅРѕРіРѕ РїР»Р°РЅСѓ",
                    status = WorkoutAnalysisUiTextMapper.annualStatusLabel(item.status),
                    accent = item.status.statusAccent()
                )
            }
        }
    }
}

@Composable
private fun RecommendationsBlock(recommendations: List<NextWorkoutRecommendationAnalysis>) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р РµРєРѕРјРµРЅРґР°С†С–С— РЅР° РЅР°СЃС‚СѓРїРЅРµ",
                subtitle = "Р’Р°РіР° С– РїРѕРІС‚РѕСЂРµРЅРЅСЏ Р· С–СЃРЅСѓСЋС‡РѕС— Р»РѕРіС–РєРё"
            )
            recommendations.forEach { recommendation ->
                AnalysisRow(
                    title = recommendation.exerciseName,
                    primary = "${recommendation.recommendedWeight.formatWeight()} РєРі В· ${recommendation.recommendedSets} x ${recommendation.recommendedReps}",
                    secondary = recommendation.reason,
                    status = "РќР°СЃС‚СѓРїРЅРёР№ СЃРµС‚",
                    accent = AccentAi
                )
            }
        }
    }
}

@Composable
private fun SystemInsightBlock(text: String) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РЎРёСЃС‚РµРјРЅРёР№ С„С–РґР±РµРє",
                subtitle = "Р—Р±РµСЂРµР¶РµРЅРёР№ AI-РІРёСЃРЅРѕРІРѕРє"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentAi.copy(alpha = 0.075f))
                    .border(1.dp, AccentAi.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(13.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = AccentAi,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AnalysisRow(
    title: String,
    primary: String,
    secondary: String,
    status: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SystemSurfaceGlass.copy(alpha = 0.58f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = primary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        SystemStatusChip(text = status, accent = accent, active = true)
    }
}

@Composable
private fun WorkoutAnalysisLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentAi)
    }
}

@Composable
private fun WorkoutAnalysisMessage(
    title: String,
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "РќР°Р·Р°Рґ", tint = TextSecondary)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        DarkGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                SystemButton(
                    text = "РћРЅРѕРІРёС‚Рё",
                    icon = Icons.Filled.Refresh,
                    onClick = onRetry,
                    accent = AccentAi,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun ExerciseProgressStatus.statusAccent(): Color =
    when (this) {
        ExerciseProgressStatus.Improved -> AccentSuccess
        ExerciseProgressStatus.Stable -> AccentPrimary
        ExerciseProgressStatus.Decreased -> AccentWarning
    }

private fun AnnualProgressStatus.statusAccent(): Color =
    when (this) {
        AnnualProgressStatus.OnPlan -> AccentSuccess
        AnnualProgressStatus.BelowPlan -> AccentWarning
        AnnualProgressStatus.AbovePlan -> AccentPrimary
        AnnualProgressStatus.NoPlan -> TextMuted
    }

private fun Long.formatDate(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("uk")))

private fun Double?.formatSignedWeight(): String =
    this?.let {
        val prefix = if (it > 0.0) "+" else ""
        "$prefix${it.formatWeight()} РєРі"
    } ?: "вЂ”"

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
