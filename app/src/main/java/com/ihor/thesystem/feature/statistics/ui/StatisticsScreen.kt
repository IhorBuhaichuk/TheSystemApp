package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.feature.statistics.ui.components.*
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogHeightDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogWeightDialog
import com.ihor.thesystem.feature.statistics.viewmodel.*

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundDeep)) {
        
        AnimatedStatisticsBackground()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 120.dp)
            ) {
                StatisticsHeader(navController)

                Spacer(modifier = Modifier.height(32.dp))

                StatisticsTabSelector(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                when (val state = uiState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = NeonCyan) }
                    }

                    is UiState.Content -> {
                        Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                            when (tab) {
                                0 -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        PlayerStatsHeaderPremium(data = state.data)

                                        PlayerMetricsGrid(
                                            weight = state.data.currentWeight.toString(),
                                            height = state.data.currentHeight.toInt().toString(),
                                            onWeightClick = { viewModel.onOpenLogWeight() },
                                            onHeightClick = { viewModel.onOpenEditHeight() }
                                        )
                                    }
                                }
                                1 -> {
                                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                        Text(text = "МАТРИЦЯ: In Development", color = TextSecondary)
                                    }
                                }
                                2 -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        PlayerCharacterPanel(state.data)
                                        ProgressLineChartMock()
                                    }
                                }
                            }
                        }

                        // Existing business logic dialogs handling
                        when (dialogState) {
                            StatisticsDialogState.None -> Unit
                            StatisticsDialogState.LogWeight -> {
                                LogWeightDialog(
                                    currentWeight = state.data.currentWeight,
                                    onConfirm = { viewModel.onWeightConfirmed(it) },
                                    onDismiss = { viewModel.onDismissDialog() }
                                )
                            }
                            StatisticsDialogState.EditHeight -> {
                                LogHeightDialog(
                                    currentHeight = state.data.currentHeight,
                                    onConfirm = { viewModel.onHeightConfirmed(it) },
                                    onDismiss = { viewModel.onDismissDialog() }
                                )
                            }
                            else -> Unit
                        }
                    }

                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(text = "[ ERROR: ${state.message} ]", color = NeonRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsTabSelector(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("ПЕРСОНАЖ", "МАТРИЦЯ", "АНАЛІТИКА")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelSurface, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) NeonCyan else Color.Transparent,
                    animationSpec = tween(durationMillis = 300),
                    label = "TabBackground"
                )
                
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) BackgroundDeep else TextSecondary,
                    animationSpec = tween(durationMillis = 300),
                    label = "TabContent"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerMetricsGrid(
    weight: String,
    height: String,
    onWeightClick: () -> Unit,
    onHeightClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricCard(
            label = "ВАГА",
            value = weight,
            unit = "кг",
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onWeightClick
                )
        )
        MetricCard(
            label = "ЗРІСТ",
            value = height,
            unit = "см",
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onHeightClick
                )
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(PanelSurface, RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextSecondary,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun PlayerCharacterPanel(data: StatisticsUiData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelSurface, RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ХАРАКТЕРИСТИКИ ПЕРСОНАЖА",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            )
            RankBadge(rank = data.globalRank, size = 32.dp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AttributePanel(
            characterAttributes = data.characterAttributes
        )
    }
}

@Composable
private fun ProgressLineChartMock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelSurface, RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            text = "ПРОГРЕС ЗА 30 ДНІВ",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
            val width = size.width
            val height = size.height

            // Horizontal grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Bezier curve
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, height * 0.9f)
                cubicTo(
                    width * 0.3f, height * 0.8f,
                    width * 0.6f, height * 0.3f,
                    width, height * 0.1f
                )
            }

            // Fill under the curve
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(NeonGreen.copy(alpha = 0.2f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            drawPath(
                path,
                color = NeonGreen,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun StatisticsHeader(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
private fun AnimatedStatisticsBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "stats_bg")
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(BackgroundDeep)
        
        // Matrix Green Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonGreen.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.2f + (size.height * 0.1f * colorShift)),
                radius = 500.dp.toPx()
            )
        )
        
        // Cyber Blue Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.8f - (size.height * 0.1f * colorShift)),
                radius = 600.dp.toPx()
            )
        )
    }
}
