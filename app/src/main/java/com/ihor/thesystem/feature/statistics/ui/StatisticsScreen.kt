package com.ihor.thesystem.feature.statistics.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ihor.thesystem.core.ui.components.*
import com.ihor.thesystem.feature.statistics.ui.components.*
import com.ihor.thesystem.feature.statistics.ui.dialogs.*
import com.ihor.thesystem.feature.statistics.viewmodel.*

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState     by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        // ── Header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = "СТАТИСТИКА",
                color         = NeonGreen,
                fontFamily    = RajdhaniFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 18.sp,
                letterSpacing = 3.sp
            )
            Text(
                text       = "МАТРИЦЯ ПРОГРЕСІЇ",
                color      = TextSecondary,
                fontFamily = RajdhaniFamily,
                fontSize   = 10.sp
            )
        }

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
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding      = PaddingValues(bottom = 16.dp)
                ) {
                    // ── Player header card ─────────────────────────────
                    item {
                        PlayerStatsHeader(data = data)
                    }

                    // ── Annual Matrix Button ───────────────────────────
                    item {
                        Button(
                            onClick = { navController.navigate(Routes.AnnualMatrix.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .sciPanel(
                                    borderColor = NeonGreen,
                                    backgroundColor = NeonGreen.copy(alpha = 0.1f),
                                    cornerCut = 8.dp
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = "РІЧНА МАТРИЦЯ ПРОГРЕСІЇ",
                                color = NeonGreen,
                                fontFamily = TekoFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    // ── Weight Chart section ───────────────────────────
                    if (data.weightHistory.size >= 2) {
                        item {
                            WeightProgressChart(
                                history = data.weightHistory,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // ── Matrix section title ───────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text          = "МАТРИЦЯ ПРОГРЕСІЇ",
                                color         = NeonGreen,
                                fontFamily    = RajdhaniFamily,
                                fontWeight    = FontWeight.Bold,
                                fontSize      = 12.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text       = "TAP = LOG | FLAG = SETUP",
                                color      = TextSecondary.copy(alpha = 0.5f),
                                fontFamily = RajdhaniFamily,
                                fontSize   = 8.sp
                            )
                        }
                    }

                    // ── Matrix entries ─────────────────────────────────
                    if (data.matrixEntries.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = "[ МАТРИЦЯ ПОРОЖНЯ ]",
                                    color      = TextSecondary,
                                    fontFamily = RajdhaniFamily,
                                    fontSize   = 12.sp
                                )
                            }
                        }
                    } else {
                        items(
                            items = data.matrixEntries,
                            key   = { it.exerciseId }
                        ) { entry ->
                            MatrixEntryCard(
                                entry        = entry,
                                onCardClick  = { viewModel.onOpenLogSets(entry) },
                                onSetupClick = { viewModel.onOpenSetup(entry) }
                            )
                        }
                    }
                }

                // ── Dialogs ───────────────────────────────────────────
                when (val dialog = dialogState) {
                    is StatisticsDialogState.SetupMatrix -> {
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
                    is StatisticsDialogState.LogWorkoutSets -> {
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
                    StatisticsDialogState.None -> Unit
                    else -> Unit
                }
            }

            is UiState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "[ ПОМИЛКА: ${state.message} ]",
                        color      = NeonRed,
                        fontFamily = RajdhaniFamily
                    )
                }
            }
        }
    }
}
