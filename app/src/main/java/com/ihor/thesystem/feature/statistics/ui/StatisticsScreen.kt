package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.feature.statistics.ui.components.*
import com.ihor.thesystem.feature.statistics.ui.dialogs.EditWeightDialog
import com.ihor.thesystem.feature.statistics.viewmodel.*

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

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
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                fontSize      = 18.sp,
                letterSpacing = 3.sp
            )
            Text(
                text       = "МАТРИЦЯ ПРОГРЕСІЇ",
                color      = TextSecondary,
                fontFamily = FontFamily.Monospace,
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
                                fontFamily    = FontFamily.Monospace,
                                fontWeight    = FontWeight.Bold,
                                fontSize      = 12.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text       = "LONG PRESS = РЕДАГУВАТИ",
                                color      = TextSecondary.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
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
                                    fontFamily = FontFamily.Monospace,
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
                                entry       = entry,
                                onLongPress = { viewModel.onEditWeightTap(entry) }
                            )
                        }
                    }

                    item {
                        TonnageChartCanvas(
                            entries = data.tonnageStats,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // ── Dialog ─────────────────────────────────────────────
                when (val dialog = dialogState) {
                    is StatisticsDialogState.EditWeight -> {
                        EditWeightDialog(
                            entry     = dialog.entry,
                            onConfirm = { weight ->
                                viewModel.onWeightConfirmed(dialog.entry.exerciseId, weight)
                            },
                            onDismiss = { viewModel.onDismissDialog() }
                        )
                    }
                    StatisticsDialogState.None -> Unit
                }
            }

            is UiState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "[ ПОМИЛКА: ${(state as UiState.Error).message} ]",
                        color      = NeonRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}