package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.feature.statistics.ui.components.*
import com.ihor.thesystem.feature.statistics.viewmodel.*

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState     by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.uiText.asString(context)
                    )
                }
                UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020408))) {
        // Shared dynamic background for consistency
        AnimatedStatisticsBackground()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Custom Premium Header ─────────────────────────────────────
                StatisticsHeader(navController)

                when (val state = uiState) {
                    is UiState.Loading -> {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = NeonGreen) }
                    }

                    is UiState.Content -> {
                        val data = state.data

                        LazyColumn(
                            modifier            = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding      = PaddingValues(bottom = 100.dp)
                        ) {
                            // ── Player Performance Card ─────────────────────────────
                            item {
                                PlayerStatsHeaderPremium(data = data)
                            }

                            // ── Matrix Controls ───────────────────────────
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    PremiumActionButton(
                                        text = "МАТРИЦЯ РОКУ",
                                        icon = Icons.Default.Grid4x4,
                                        modifier = Modifier.weight(1f),
                                        color = NeonGreen,
                                        onClick = { navController.navigate(Routes.AnnualMatrix) }
                                    )
                                }
                            }

                            // ── Weight Chart section ───────────────────────────
                            if (data.weightHistory.size >= 2) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                            .padding(16.dp)
                                    ) {
                                        WeightProgressChart(
                                            history = data.weightHistory,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        // ── Dialogs ───────────────────────────────────────────
                        when (dialogState) {
                            StatisticsDialogState.None -> Unit
                            else -> Unit // Matrix dialogs moved to ModeScreen
                        }
                    }

                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "[ ERROR: ${state.message} ]", color = NeonRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsHeader(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Text(
            text = "АНАЛІТИКА",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
        )

        Box(modifier = Modifier.size(40.dp)) // Spacer to keep title centered
    }
}

@Composable
private fun PremiumActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
private fun AnimatedStatisticsBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "stats_bg")
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF020408))
        
        // Matrix Green Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00FF94).copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.2f + (size.height * 0.1f * colorShift)),
                radius = 500.dp.toPx()
            )
        )
        
        // Cyber Blue Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00B2FF).copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.8f - (size.height * 0.1f * colorShift)),
                radius = 600.dp.toPx()
            )
        )
    }
}
