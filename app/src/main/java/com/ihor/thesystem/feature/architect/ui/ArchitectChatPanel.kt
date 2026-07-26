package com.ihor.thesystem.feature.architect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.asString
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.data.remote.ai.AiAvailabilityState
import com.ihor.thesystem.domain.model.AiArchitectInsight
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectUiState
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
internal fun ChatPanel(
    uiState: ArchitectUiState,
    chatHistory: List<ChatMessage>,
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
    onAnalyzeClick: () -> Unit,
    onApplyClick: (List<AiWorkoutRecommendation>) -> Unit,
    onSendMessage: (String) -> Unit,
    aiAvailability: AiAvailabilityState
) {
    val colors = SystemTheme.colors
    val aiAvailable = aiAvailability == AiAvailabilityState.CONFIGURED
    val unavailableText = aiAvailability.unavailableDescription()
    DarkGlassCard(contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Чат",
                subtitle = if (aiAvailable) "Додатковий канал" else aiAvailability.shortStatus()
            )
            if (!aiAvailable) {
                EmptyAiBlock(text = unavailableText)
            }
            ChatModeSwitch(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(SystemTheme.shapes.medium))
                    .background(colors.surfaceGlassSoft)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(SystemTheme.shapes.medium))
            ) {
                if (selectedMode == 0) {
                    ArchitectThreadView(
                        uiState = uiState,
                        aiAvailable = aiAvailable,
                        onAnalyzeClick = onAnalyzeClick,
                        onApplyClick = onApplyClick
                    )
                } else {
                    LiveChatView(
                        history = chatHistory,
                        sessionId = 0L,
                        onSendMessage = { _, text -> onSendMessage(text) },
                        enabled = aiAvailable,
                        disabledReason = unavailableText,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatModeSwitch(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ChatModeButton(
            text = "Архітектор",
            selected = selectedMode == 0,
            onClick = { onModeSelected(0) },
            modifier = Modifier.weight(1f)
        )
        ChatModeButton(
            text = "Тренер",
            selected = selectedMode == 1,
            onClick = { onModeSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChatModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(if (selected) colors.accentAi.copy(alpha = 0.13f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) colors.accentAi.copy(alpha = 0.28f) else Color.Transparent,
                shape
            )
            .systemClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (selected) colors.textPrimary else colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArchitectThreadView(
    uiState: ArchitectUiState,
    aiAvailable: Boolean,
    onAnalyzeClick: () -> Unit,
    onApplyClick: (List<AiWorkoutRecommendation>) -> Unit
) {
    val colors = SystemTheme.colors
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(SystemItemSpacing)
    ) {
        items(uiState.messages, key = { it.id }) { message ->
            ArchitectChatBubble(
                message = message,
                onAnalyzeClick = onAnalyzeClick,
                onApplyClick = onApplyClick,
                isAnalyzeEnabled = aiAvailable && !uiState.analysisAlreadySent && !uiState.isLoading
            )
        }

        if (uiState.isLoading) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = colors.accentAi,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Архітектор формує відповідь",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchitectChatBubble(
    message: ChatMessage,
    onAnalyzeClick: () -> Unit,
    onApplyClick: (List<AiWorkoutRecommendation>) -> Unit,
    isAnalyzeEnabled: Boolean
) {
    val colors = SystemTheme.colors
    val isUser = message.role == ChatRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val isSystem = message.role == ChatRole.SYSTEM
    val accent = if (message.role == ChatRole.AI) colors.accentAi else colors.accentPrimary
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else if (isSystem) {
        RoundedCornerShape(SystemTheme.shapes.medium)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier.fillMaxWidth(if (isSystem) 1f else 0.9f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (message.role) {
                    ChatRole.USER -> "Ти"
                    ChatRole.AI -> "Архітектор"
                    ChatRole.SYSTEM -> "Система"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSystem) colors.textSecondary else if (isUser) colors.textMuted else accent,
                    fontWeight = FontWeight.Bold
                )
            )
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        when {
                            isSystem -> colors.overlayLight
                            isUser -> colors.overlayMedium
                            else -> accent.copy(alpha = 0.07f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isSystem -> colors.borderSubtle
                            isUser -> colors.borderSubtle
                            else -> accent.copy(alpha = 0.20f)
                        },
                        shape
                    )
                    .padding(SystemItemSpacing),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val architectInsight = message.architectInsight?.takeIf { it.hasSignal }
                if (architectInsight == null) {
                    Text(
                        text = message.text.asString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                } else {
                    ArchitectInsightPayload(insight = architectInsight)
                }
                if (message.isSystemCorrectionNotice()) {
                    SystemStatusChip(
                        text = "Скориговано системою",
                        accent = colors.accentWarning,
                        active = true
                    )
                }
                if (message.recommendations.isNotEmpty()) {
                    RecommendationList(recommendations = message.recommendations)
                }
            }

            if (message.isActionable) {
                if (message.role == ChatRole.SYSTEM) {
                    SystemButton(
                        text = "Аналізувати дані",
                        onClick = onAnalyzeClick,
                        icon = Icons.Filled.Psychology,
                        accent = colors.accentAi,
                        enabled = isAnalyzeEnabled
                    )
                } else if (message.role == ChatRole.AI) {
                    SystemButton(
                        text = "Застосувати рекомендації",
                        onClick = { onApplyClick(message.recommendations) },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        accent = colors.accentPrimary,
                        enabled = message.recommendations.isNotEmpty()
                    )
                }
            }
        }
    }
}

private fun ChatMessage.isSystemCorrectionNotice(): Boolean =
    role == ChatRole.SYSTEM &&
        (text as? MessageText.DynamicString)?.value == SYSTEM_AI_CORRECTION_TEXT

@Composable
private fun ArchitectInsightPayload(insight: AiArchitectInsight) {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (insight.weeklyInsight.isNotBlank()) {
            InsightLine(label = "Тенденція", text = insight.weeklyInsight, accent = colors.accentAi)
        }
        if (insight.recoveryRisk.isNotBlank()) {
            InsightLine(label = "Ризик", text = insight.recoveryRisk, accent = colors.accentWarning)
        }
        insight.actionableSuggestions.take(3).forEachIndexed { index, suggestion ->
            InsightLine(
                label = "Дія ${index + 1}",
                text = suggestion,
                accent = colors.accentPrimary
            )
        }
    }
}

@Composable
private fun InsightLine(
    label: String,
    text: String,
    accent: Color
) {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = accent,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecommendationList(recommendations: List<AiWorkoutRecommendation>) {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        recommendations.forEach { recommendation ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary)
                )
                Text(
                    text = "Вправа №${recommendation.exerciseId}: ${formatWeight(recommendation.weight)} кг · ${recommendation.sets} × ${recommendation.reps}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatWeight(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private const val SYSTEM_AI_CORRECTION_TEXT = "Система скоригувала пораду тренера"
