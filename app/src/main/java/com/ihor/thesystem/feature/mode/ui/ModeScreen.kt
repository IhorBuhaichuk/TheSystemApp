package com.ihor.thesystem.feature.mode.ui

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.feature.mode.ui.components.*
import com.ihor.thesystem.feature.mode.ui.dialogs.ConfirmAdvanceDialog
import com.ihor.thesystem.feature.mode.ui.dialogs.SyncAnchorDialog
import com.ihor.thesystem.feature.mode.viewmodel.ModeDialogState
import com.ihor.thesystem.feature.mode.viewmodel.ModeEvent
import com.ihor.thesystem.feature.mode.viewmodel.ModeViewModel
import com.ihor.thesystem.feature.statistics.ui.dialogs.LogWorkoutSetsDialog
import com.ihor.thesystem.feature.statistics.ui.dialogs.SetupMatrixDialog

@Composable
fun ModeScreen(
    navController: NavHostController,
    viewModel: ModeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState     by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ModeEvent.DayAdvanced ->
                    snackbarHostState.showSnackbar("День завершено. Новий цикл розпочато!")
                ModeEvent.CycleSynced ->
                    snackbarHostState.showSnackbar("Цикл синхронізовано з календарем")
                ModeEvent.LevelUp ->
                    Toast.makeText(context, "LEVEL UP!", Toast.LENGTH_SHORT).show()
                ModeEvent.PenaltyActivated ->
                    Toast.makeText(context, "ШТРАФНА ЗОНА АКТИВОВАНА", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.uiText.asString(context))
                }
                UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor   = NeonCyan,
                    actionColor    = NeonGold,
                    modifier = Modifier.border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
            // Shared dynamic background
            AnimatedModeBackground()

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = NeonGold) }
                }

                is UiState.Content -> {
                    val data = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── Header ────────────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Text(
                                text = "РЕЖИМ ЦИКЛУ",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${data.days.size}-денний протокол",
                                color = Color.White.copy(alpha = 0.4f),
                                fontFamily = RajdhaniFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 120.dp), // Space for sticky button
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // ── Cycle Day Selector ────────────────────────
                            CycleDaySelector(
                                days = data.days,
                                onTap = { viewModel.onCycleDayTap(it) },
                                onLongPress = { viewModel.onCycleDayLongPress(it) }
                            )

                            // ── Penalty Warning ───────────────────────────
                            if (data.isPenaltyActive) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PanelSurface)
                                        .border(1.dp, NeonRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NeonRed.copy(alpha = 0.1f), CircleShape)
                                            .border(1.dp, NeonRed.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("!", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                    Column {
                                        Text(
                                            text       = "ШТРАФНА ЗОНА АКТИВНА",
                                            color      = NeonRed,
                                            fontFamily = TekoFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text       = "Ваги знижені. Виконай 2 Main Quest для відновлення.",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontFamily = RajdhaniFamily,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // ── Active Day Card ───────────────────────────
                            data.activeDayData?.let { dayData ->
                                ActiveDayCard(
                                    data = dayData,
                                    onOpenLogSets = { viewModel.onOpenLogSets(it) },
                                    onOpenSetup = { viewModel.onOpenSetup(it) }
                                )
                            }
                        }
                    }

                    // ── Sticky Next Day Button ───────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        NextDayButton(
                            currentDay = data.currentCycleDay,
                            onClick    = { viewModel.onNextDayTap() }
                        )
                    }

                    // ── Dialogs ───────────────────────────────────────
                    when (val dialog = dialogState) {
                        is ModeDialogState.ConfirmAdvance -> {
                            ConfirmAdvanceDialog(
                                currentDay      = data.currentCycleDay,
                                onConfirm       = { viewModel.onConfirmAdvance() },
                                onForceComplete = { viewModel.onForceCompleteDay() },
                                onDismiss       = { viewModel.onDismissDialog() }
                            )
                        }
                        is ModeDialogState.SyncAnchor -> {
                            SyncAnchorDialog(
                                dayNumber = dialog.day,
                                onConfirm = { viewModel.onConfirmSync(dialog.day) },
                                onDismiss = { viewModel.onDismissDialog() }
                            )
                        }
                        is ModeDialogState.SetupMatrix -> {
                            SetupMatrixDialog(
                                exerciseName = dialog.entry.exerciseName,
                                initialStart = dialog.startWeight,
                                initialTarget = dialog.targetWeight,
                                onConfirm = { start, target ->
                                    viewModel.onConfirmSetup(dialog.entry.exerciseId, start, target)
                                },
                                onDismiss = { viewModel.onDismissDialog() }
                            )
                        }
                        is ModeDialogState.LogWorkoutSets -> {
                            LogWorkoutSetsDialog(
                                exerciseName = dialog.entry.exerciseName,
                                sets = dialog.sets,
                                onUpdate = { id, w, r -> viewModel.updateSetInput(id, w, r) },
                                onAdd = { viewModel.addSet() },
                                onRemove = { viewModel.removeSet() },
                                onSave = { feedback ->
                                    viewModel.onLogSetsConfirmed(dialog.entry.exerciseId, dialog.sets, feedback)
                                },
                                onDismiss = { viewModel.onDismissDialog() },
                                existingLog = dialog.existingLog
                            )
                        }
                        else -> Unit
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "[ SYSTEM ERROR: ${(state as UiState.Error).message} ]",
                            color      = NeonRed,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedModeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mode_bg")
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Neon Gold Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonGold.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(size.width * 0.15f + (size.width * 0.1f * colorShift), size.height * 0.25f),
                radius = 700.dp.toPx()
            )
        )
        
        // Neon Cyan Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(size.width * 0.85f - (size.width * 0.1f * colorShift), size.height * 0.75f),
                radius = 800.dp.toPx()
            )
        )
    }
}
