package com.ihor.thesystem.feature.mode.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.mode.ui.components.*
import com.ihor.thesystem.feature.mode.ui.dialogs.ConfirmAdvanceDialog
import com.ihor.thesystem.feature.mode.viewmodel.ModeDialogState
import com.ihor.thesystem.feature.mode.viewmodel.ModeEvent
import com.ihor.thesystem.feature.mode.viewmodel.ModeViewModel
import kotlinx.coroutines.flow.collect

@Composable
fun ModeScreen(
    navController: NavHostController,
    viewModel: ModeViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ModeEvent.DayAdvanced ->
                    snackbarHostState.showSnackbar("День завершено. Новий цикл розпочато!")
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeep,
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = PanelSurface,
                    contentColor   = NeonCyan,
                    actionColor    = NeonGold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDeep)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text          = "РЕЖИМ ЦИКЛУ",
                    color         = NeonGold,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 18.sp,
                    letterSpacing = 3.sp
                )
                Text(
                    text       = "4-ДЕННИЙ ПРОТОКОЛ",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 10.sp
                )
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = NeonGold) }
                }

                is UiState.Content -> {
                    val data = state.data

                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ── Cycle Day Selector ────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sciPanel(PanelBorder, PanelSurface.copy(alpha = 0.9f), 12.dp)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text          = "ПОТОЧНИЙ ЦИКЛ",
                                color         = TextSecondary,
                                fontFamily    = FontFamily.Monospace,
                                fontSize      = 10.sp,
                                letterSpacing = 2.sp
                            )
                            CycleDaySelector(days = data.days)
                        }

                        // ── Penalty Warning ───────────────────────────
                        if (data.isPenaltyActive) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sciPanel(NeonRed, NeonRed.copy(alpha = 0.1f), 8.dp)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("⚠", color = NeonRed, fontSize = 18.sp)
                                Column {
                                    Text(
                                        text       = "ШТРАФНА ЗОНА АКТИВНА",
                                        color      = NeonRed,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 12.sp
                                    )
                                    Text(
                                        text       = "Ваги знижені. Виконай 2 Main Quest для відновлення.",
                                        color      = NeonRed.copy(alpha = 0.7f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize   = 10.sp
                                    )
                                }
                            }
                        }

                        // ── Active Day Card ───────────────────────────
                        data.activeDayData?.let { dayData ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sciPanel(NeonCyan.copy(0.5f), PanelSurface, 12.dp)
                                    .padding(14.dp)
                            ) {
                                ActiveDayCard(data = dayData)
                            }
                        }

                        // ── Next Day Button ───────────────────────────
                        NextDayButton(
                            currentDay = data.currentCycleDay,
                            onClick    = { viewModel.onNextDayTap() }
                        )

                        Spacer(Modifier.height(8.dp))
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
                        else -> Unit
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(200.dp),
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
}