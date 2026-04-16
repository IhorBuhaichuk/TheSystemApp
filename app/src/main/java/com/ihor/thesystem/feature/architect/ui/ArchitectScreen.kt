package com.ihor.thesystem.feature.architect.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.components.GlitchText
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectViewModel
import com.ihor.thesystem.feature.architect.viewmodel.ArchitectUiState
import kotlinx.coroutines.launch

@Composable
fun ArchitectScreen(
    viewModel: ArchitectViewModel = hiltViewModel(),
    onAcknowledge: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.uiText.asString(context), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        // ── Header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            GlitchText(
                text = "AI INTERFACE v1.0",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = TekoFamily,
                    letterSpacing = 4.sp
                )
            )
        }

        // ── Tabs (Завдання 2) ─────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NeonCyan
                    )
                }
            },
            divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("АРХІТЕКТОР", fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { 
                    selectedTabIndex = 1
                    // Завантаження історії для сесії 0L (або поточної)
                    viewModel.loadChatHistory(0L) 
                },
                text = { Text("ТРЕНЕР", fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTabIndex == 0) {
                ArchitectView(uiState, viewModel)
            } else {
                LiveChatView(
                    history = chatHistory,
                    sessionId = 0L,
                    onSendMessage = { sessionId, text -> viewModel.sendMessage(sessionId, text) }
                )
            }
        }

        // ── Bottom Controls ───────────────────────────────────────
        Button(
            onClick = onAcknowledge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp)
                .sciPanel(borderColor = NeonCyan.copy(0.3f), backgroundColor = Color.Transparent, cornerCut = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                text = "[ ПОВЕРНУТИСЬ В СИСТЕМУ ]",
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }
    }
}

@Composable
private fun ArchitectView(
    uiState: ArchitectUiState,
    viewModel: ArchitectViewModel
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        items(uiState.messages, key = { it.id }) { message ->
            ChatBubble(
                message = message,
                onAnalyzeClick = { viewModel.sendForAnalysis() },
                onApplyClick = { viewModel.applyRecommendations(it) },
                isAnalyzeEnabled = !uiState.analysisAlreadySent
            )
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    GlitchText(
                        text = "АРХІТЕКТОР ФОРМУЄ ВІДПОВІДЬ...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeonCyan,
                            fontFamily = RajdhaniFamily
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onAnalyzeClick: () -> Unit,
    onApplyClick: (List<AiWorkoutRecommendation>) -> Unit,
    isAnalyzeEnabled: Boolean = true
) {
    val alignment = when (message.role) {
        ChatRole.USER -> Alignment.CenterEnd
        ChatRole.AI   -> Alignment.CenterStart
        ChatRole.SYSTEM -> Alignment.CenterStart
    }

    val bubbleModifier = when (message.role) {
        ChatRole.USER -> Modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(Color(0xFF2D2D2D))
            .padding(12.dp)
        
        ChatRole.AI -> Modifier
            .sciPanel(
                borderColor = Color(0xFFBC00FF).copy(0.6f),
                backgroundColor = PanelSurface,
                cornerCut = 10.dp
            )
            .padding(14.dp)
            
        ChatRole.SYSTEM -> Modifier
            .sciPanel(
                borderColor = NeonCyan.copy(0.4f),
                backgroundColor = NeonCyan.copy(0.05f),
                cornerCut = 4.dp
            )
            .padding(12.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (message.role == ChatRole.USER) Alignment.End else Alignment.Start
        ) {
            Text(
                text = message.role.name,
                fontFamily = TekoFamily,
                fontSize = 10.sp,
                color = when(message.role) {
                    ChatRole.AI -> Color(0xFFBC00FF)
                    ChatRole.USER -> Color.Gray
                    ChatRole.SYSTEM -> NeonCyan
                },
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Column(modifier = bubbleModifier) {
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontFamily = RajdhaniFamily,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )

                if (message.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "НОВІ ДИРЕКТИВИ:",
                        fontFamily = TekoFamily,
                        color = NeonGold,
                        fontSize = 12.sp
                    )
                    message.recommendations.forEach { rec ->
                        Text(
                            text = "• Вправа #${rec.exerciseId}: ${rec.weight}кг x ${rec.reps}",
                            fontFamily = RajdhaniFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (message.isActionable) {
                Spacer(modifier = Modifier.height(8.dp))
                if (message.role == ChatRole.SYSTEM) {
                    CyberButton(
                        text = "НАДІСЛАТИ РЕЗУЛЬТАТИ",
                        color = NeonCyan,
                        onClick = onAnalyzeClick,
                        enabled = isAnalyzeEnabled
                    )
                } else if (message.role == ChatRole.AI) {
                    CyberButton(
                        text = "ПРОПИСАТИ В СИСТЕМУ",
                        color = NeonGold,
                        onClick = { onApplyClick(message.recommendations) }
                    )
                }
            }
        }
    }
}

@Composable
fun CyberButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .sciPanel(borderColor = if (enabled) color else color.copy(0.2f), backgroundColor = color.copy(0.1f), cornerCut = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontFamily = RajdhaniFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (enabled) color else color.copy(0.4f)
        )
    }
}
