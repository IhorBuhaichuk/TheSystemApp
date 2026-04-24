package com.ihor.thesystem.feature.architect.ui

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.ihor.thesystem.feature.architect.ui.LiveChatView
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
                UiEvent.NavigateBack -> {
                    onAcknowledge()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020408))) {
        // Shared dynamic background
        AnimatedArchitectBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header ────────────────────────────────────────────────
            ArchitectHeader(onBack = onAcknowledge)

            // ── Tabs ─────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = GlitchPink,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = GlitchPink
                        )
                    }
                },
                divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { 
                        Text(
                            text = "АРХІТЕКТОР", 
                            fontFamily = RajdhaniFamily, 
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) GlitchPink else Color.White.copy(alpha = 0.5f)
                        ) 
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { 
                        selectedTabIndex = 1
                        viewModel.loadChatHistory(0L) 
                    },
                    text = { 
                        Text(
                            text = "ТРЕНЕР", 
                            fontFamily = RajdhaniFamily, 
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) GlitchPink else Color.White.copy(alpha = 0.5f)
                        ) 
                    }
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
            Surface(
                onClick = onAcknowledge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = GlitchPink.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, GlitchPink.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "ПОВЕРНУТИСЬ В СИСТЕМУ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = GlitchPink,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchitectHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        GlitchText(
            text = "AI INTERFACE",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = TekoFamily,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
        )

        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun AnimatedArchitectBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF020408))
        
        // Glitch Pink Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GlitchPink.copy(alpha = 0.07f), Color.Transparent),
                center = Offset(size.width * 0.1f + (size.width * 0.1f * colorShift), size.height * 0.2f),
                radius = 700.dp.toPx()
            )
        )
        
        // Neon Cyan Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.07f), Color.Transparent),
                center = Offset(size.width * 0.9f - (size.width * 0.1f * colorShift), size.height * 0.8f),
                radius = 800.dp.toPx()
            )
        )
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
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
            .padding(16.dp)
        
        ChatRole.AI -> Modifier
            .sciPanel(
                borderColor = GlitchPink.copy(0.4f),
                backgroundColor = Color.White.copy(alpha = 0.03f),
                cornerCut = 12.dp
            )
            .padding(16.dp)
            
        ChatRole.SYSTEM -> Modifier
            .background(NeonCyan.copy(0.05f), RoundedCornerShape(8.dp))
            .border(1.dp, NeonCyan.copy(0.2f), RoundedCornerShape(8.dp))
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
                    ChatRole.AI -> GlitchPink
                    ChatRole.USER -> Color.White.copy(alpha = 0.3f)
                    ChatRole.SYSTEM -> NeonCyan
                },
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp, end = 4.dp)
            )

            Column(modifier = bubbleModifier) {
                val contentText = message.text.asString()
                Text(
                    text = contentText,
                    color = Color.White,
                    fontFamily = RajdhaniFamily,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                if (message.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ВЕРДИКТ АРХІТЕКТОРА:",
                        fontFamily = TekoFamily,
                        color = NeonGold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    message.recommendations.forEach { rec ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Box(modifier = Modifier.size(4.dp).background(NeonGold, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Вправа #${rec.exerciseId}: ${rec.weight}кг x ${rec.reps}",
                                fontFamily = RajdhaniFamily,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (message.isActionable) {
                Spacer(modifier = Modifier.height(12.dp))
                if (message.role == ChatRole.SYSTEM) {
                    CyberButton(
                        text = "АНАЛІЗУВАТИ ДАНІ",
                        color = NeonCyan,
                        onClick = onAnalyzeClick,
                        enabled = isAnalyzeEnabled
                    )
                } else if (message.role == ChatRole.AI) {
                    CyberButton(
                        text = "ІНТЕГРУВАТИ В СИСТЕМУ",
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
    Surface(
        onClick = if (enabled) onClick else ({}),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) color.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f),
        border = BorderStroke(1.dp, if (enabled) color.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (enabled) color else Color.White.copy(alpha = 0.2f),
                letterSpacing = 1.sp
            )
        }
    }
}
