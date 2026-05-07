package com.ihor.thesystem.feature.architect.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.theme.AccentAi
import com.ihor.thesystem.core.theme.AccentAiSoft
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentPrimarySoft
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemBackgroundSecondary
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.feature.architect.viewmodel.AiDashboardUiState
import com.ihor.thesystem.feature.architect.viewmodel.AiRecommendationUiModel
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectUiState
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ArchitectScreen(
    viewModel: ArchitectViewModel = hiltViewModel(),
    onAcknowledge: () -> Unit,
    onOpenAnnualProgression: () -> Unit = {},
    onOpenWorkoutAnalysis: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    var selectedChatMode by remember { mutableIntStateOf(0) }

    RefreshOnResume(viewModel::refreshForCurrentData)

    LaunchedEffect(Unit) {
        viewModel.loadChatHistory(0L)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.uiText.asString(context), Toast.LENGTH_SHORT).show()
                }
                UiEvent.NavigateBack -> onAcknowledge()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        AiSystemBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            AiHeader()
            AiModulesBlock(
                hasWorkoutContext = uiState.lastWorkoutContext != null,
                isLoading = uiState.isLoading,
                onOpenAnnualProgression = onOpenAnnualProgression,
                onAnalyzeWorkout = onOpenWorkoutAnalysis
            )
            ShortConclusionBlock(dashboardState)
            LastRecommendationBlock(dashboardState.lastRecommendation)
            ChatPanel(
                uiState = uiState,
                chatHistory = chatHistory,
                selectedMode = selectedChatMode,
                onModeSelected = { selectedChatMode = it },
                onAnalyzeClick = viewModel::sendForAnalysis,
                onApplyClick = viewModel::applyRecommendations,
                onSendMessage = { message -> viewModel.sendMessage(0L, message) }
            )
        }
    }
}

@Composable
private fun AiHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = "АІ",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            text = "Системні модулі",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
    }
}

@Composable
private fun AiModulesBlock(
    hasWorkoutContext: Boolean,
    isLoading: Boolean,
    onOpenAnnualProgression: () -> Unit,
    onAnalyzeWorkout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AiModuleCard(
            title = "План річної прогресії",
            description = "Прогноз на 12 місяців по вибраних вправах",
            icon = Icons.Filled.BarChart,
            status = "Графік",
            enabled = true,
            onClick = onOpenAnnualProgression
        )
        AiModuleCard(
            title = "Аналіз тренування",
            description = "Фідбек, рекомендації ваги та повторень",
            icon = Icons.Filled.Psychology,
            status = if (hasWorkoutContext) "Готово" else "Немає логу",
            enabled = hasWorkoutContext && !isLoading,
            onClick = onAnalyzeWorkout
        )
        AiModuleCard(
            title = "Корекція циклу",
            description = "Перебудова навантаження після пропусків",
            icon = Icons.Filled.Loop,
            status = "Не підключено",
            enabled = false,
            onClick = {}
        )
        AiModuleCard(
            title = "План на завтра",
            description = "Рекомендований фокус на наступний день",
            icon = Icons.Filled.CalendarMonth,
            status = "Не підключено",
            enabled = false,
            onClick = {}
        )
    }
}

@Composable
private fun AiModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    status: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    DarkGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        active = enabled,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(AccentAi.copy(alpha = if (enabled) 0.12f else 0.055f))
                    .border(1.dp, AccentAi.copy(alpha = if (enabled) 0.26f else 0.11f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) AccentAi else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (enabled) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SystemStatusChip(
                    text = status,
                    accent = if (enabled) AccentAi else TextMuted,
                    active = enabled
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (enabled) AccentAi else TextMuted.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ShortConclusionBlock(state: AiDashboardUiState) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Короткий висновок",
                subtitle = "На основі системних метрик"
            )
            if (state.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = AccentAi,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Система читає метрики",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            } else {
                SystemInsightText(
                    text = state.shortConclusion.ifBlank { "Даних для висновку поки недостатньо." },
                    icon = Icons.Filled.AutoAwesome
                )
            }
        }
    }
}

@Composable
private fun LastRecommendationBlock(recommendation: AiRecommendationUiModel?) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Остання рекомендація",
                subtitle = "Збережена в матриці прогресії"
            )
            if (recommendation == null) {
                EmptyAiBlock(text = "Історії AI-рекомендацій ще немає.")
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentAi.copy(alpha = 0.075f))
                        .border(1.dp, AccentAi.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                        .padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(AccentAiSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = AccentAi,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = recommendation.exerciseName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatRecommendationTarget(recommendation),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AccentAi,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        recommendation.feedback?.takeIf { it.isNotBlank() }?.let { feedback ->
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatPanel(
    uiState: ArchitectUiState,
    chatHistory: List<ChatMessage>,
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
    onAnalyzeClick: () -> Unit,
    onApplyClick: (List<AiWorkoutRecommendation>) -> Unit,
    onSendMessage: (String) -> Unit
) {
    DarkGlassCard(contentPadding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Чат",
                subtitle = "Додатковий канал"
            )
            ChatModeSwitch(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.18f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                if (selectedMode == 0) {
                    ArchitectThreadView(
                        uiState = uiState,
                        onAnalyzeClick = onAnalyzeClick,
                        onApplyClick = onApplyClick
                    )
                } else {
                    LiveChatView(
                        history = chatHistory,
                        sessionId = 0L,
                        onSendMessage = { _, text -> onSendMessage(text) },
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
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
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) AccentAi.copy(alpha = 0.13f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) AccentAi.copy(alpha = 0.28f) else Color.Transparent,
                RoundedCornerShape(11.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (selected) TextPrimary else TextSecondary,
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
    onAnalyzeClick: () -> Unit,
    onApplyClick: (List<AiWorkoutRecommendation>) -> Unit
) {
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
        contentPadding = PaddingValues(12.dp)
    ) {
        items(uiState.messages, key = { it.id }) { message ->
            ArchitectChatBubble(
                message = message,
                onAnalyzeClick = onAnalyzeClick,
                onApplyClick = onApplyClick,
                isAnalyzeEnabled = !uiState.analysisAlreadySent && !uiState.isLoading
            )
        }

        if (uiState.isLoading) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = AccentAi,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Архітектор формує відповідь",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
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
    val isUser = message.role == ChatRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val isSystem = message.role == ChatRole.SYSTEM
    val accent = if (message.role == ChatRole.AI) AccentAi else AccentPrimary
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else if (isSystem) {
        RoundedCornerShape(14.dp)
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
                    color = if (isSystem) TextSecondary else if (isUser) TextMuted else accent,
                    fontWeight = FontWeight.Bold
                )
            )
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        when {
                            isSystem -> Color.White.copy(alpha = 0.035f)
                            isUser -> Color.White.copy(alpha = 0.045f)
                            else -> accent.copy(alpha = 0.07f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isSystem -> BorderSubtle
                            isUser -> BorderSubtle
                            else -> accent.copy(alpha = 0.2f)
                        },
                        shape
                    )
                    .padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = message.text.asString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
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
                        accent = AccentAi,
                        enabled = isAnalyzeEnabled
                    )
                } else if (message.role == ChatRole.AI) {
                    SystemButton(
                        text = "Інтегрувати в систему",
                        onClick = { onApplyClick(message.recommendations) },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        accent = AccentPrimary,
                        enabled = message.recommendations.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationList(recommendations: List<AiWorkoutRecommendation>) {
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
                        .background(AccentPrimary)
                )
                Text(
                    text = "Вправа #${recommendation.exerciseId}: ${formatWeight(recommendation.weight)} кг · ${recommendation.sets} x ${recommendation.reps}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SystemInsightText(
    text: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AccentAi.copy(alpha = 0.075f))
            .border(1.dp, AccentAi.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(13.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentAi,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun EmptyAiBlock(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.026f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(13.dp))
            .padding(14.dp),
        style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun AiSystemBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(SystemBackground)
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    SystemBackgroundSecondary.copy(alpha = 0.92f),
                    SystemBackground,
                    Color.Black.copy(alpha = 0.98f)
                )
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AccentAi.copy(alpha = 0.055f), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.14f),
                radius = size.width * 0.7f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AccentPrimary.copy(alpha = 0.028f), Color.Transparent),
                center = Offset(size.width * 0.08f, size.height * 0.72f),
                radius = size.width * 0.82f
            )
        )
    }
}

private fun formatRecommendationTarget(recommendation: AiRecommendationUiModel): String {
    val weight = recommendation.recommendedWeight?.let { "${formatWeight(it)} кг" }
    val scheme = when {
        recommendation.recommendedSets != null && !recommendation.recommendedReps.isNullOrBlank() ->
            "${recommendation.recommendedSets} x ${recommendation.recommendedReps}"
        recommendation.recommendedSets != null -> "${recommendation.recommendedSets} підх."
        !recommendation.recommendedReps.isNullOrBlank() -> recommendation.recommendedReps
        else -> null
    }

    return listOfNotNull(weight, scheme).joinToString(" · ").ifBlank { "Рекомендація без числового таргета" }
}

private fun formatWeight(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun formatWeight(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
