package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiState

// --- Імпорти з пакета statistics ---
import com.ihor.thesystem.feature.statistics.ui.components.EmptyQuestCard
import com.ihor.thesystem.feature.statistics.ui.components.PlayerLeftPanel
import com.ihor.thesystem.feature.statistics.ui.components.QuestCard
import com.ihor.thesystem.feature.statistics.ui.components.QuestCardType
import com.ihor.thesystem.feature.statistics.ui.components.StatRightPanel
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.DebuffEditorSheet
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.EditNameDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogWeightDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.QuestChecklistSheet

// --- ВИПРАВЛЕНИЙ ІМПОРТ: SystemHeader має пакет status всередині файлу ---
import com.ihor.thesystem.feature.status.ui.components.SystemHeader

// --- Імпорти локальних діалогів із status ---
import com.ihor.thesystem.feature.status.ui.components.dialogs.LevelUpDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.PenaltyActivatedDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.PenaltyDeactivatedDialog
import com.ihor.thesystem.feature.status.viewmodel.*

@Composable
fun StatusScreen(
    navController: NavHostController,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val dialogState  by viewModel.dialogState.collectAsState()
    val allDebuffs   by viewModel.allDebuffs.collectAsState()

    // ── One-off events ────────────────────────────────────────────────
    var levelUpEvent   by remember { mutableStateOf<StatusOneOffEvent.ShowLevelUp?>(null) }
    var showPenaltyOn  by remember { mutableStateOf(false) }
    var showPenaltyOff by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StatusOneOffEvent.ShowLevelUp         -> levelUpEvent  = event
                StatusOneOffEvent.ShowPenaltyActivated   -> showPenaltyOn  = true
                StatusOneOffEvent.ShowPenaltyDeactivated -> showPenaltyOff = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
    ) {
        SystemHeader(
            onLongPress = {
                // viewModel.onSystemConfigTap()
            }
        )

        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = NeonCyan) }
            }

            is UiState.Content -> {
                val data = state.data

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerLeftPanel(
                        data         = data,
                        modifier     = Modifier.weight(0.60f),
                        onNameTap    = { viewModel.onNameTap() },
                        onDebuffEdit = { viewModel.onDebuffTap() }
                    )
                    StatRightPanel(
                        month       = "${data.currentMonth}/${data.totalMonths}",
                        weight      = "${data.currentWeight.toInt()}",
                        height      = "${data.height.toInt()}",
                        modifier    = Modifier.weight(0.40f),
                        onWeightTap = { viewModel.onWeightTap() }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    data.dailyQuest?.let { quest ->
                        QuestCard(
                            quest       = quest,
                            type        = QuestCardType.DAILY,
                            onClick     = { viewModel.onQuestTap(quest.id, isDaily = true) },
                            onLongClick = { /* TODO: редактор квесту */ }
                        )
                    } ?: EmptyQuestCard(QuestCardType.DAILY)

                    data.mainQuest?.let { quest ->
                        QuestCard(
                            quest       = quest,
                            type        = QuestCardType.MAIN,
                            onClick     = { viewModel.onQuestTap(quest.id, isDaily = false) },
                            onLongClick = { /* TODO: редактор квесту */ }
                        )
                    } ?: EmptyQuestCard(QuestCardType.MAIN)

                    Spacer(Modifier.height(6.dp))
                }

                // ── Dialogs ───────────────────────────────────────────
                when (val dialog = dialogState) {
                    is StatusDialogState.EditName -> {
                        EditNameDialog(
                            currentName = data.playerName,
                            onConfirm   = { viewModel.onNameConfirmed(it) },
                            onDismiss   = { viewModel.onDismissDialog() }
                        )
                    }
                    is StatusDialogState.LogWeight -> {
                        LogWeightDialog(
                            currentWeight = data.currentWeight,
                            onConfirm     = { viewModel.onWeightConfirmed(it) },
                            onDismiss     = { viewModel.onDismissDialog() }
                        )
                    }
                    is StatusDialogState.EditDebuffs -> {
                        DebuffEditorSheet(
                            debuffs   = allDebuffs,
                            onToggle  = { viewModel.onDebuffToggled(it) },
                            onDismiss = { viewModel.onDismissDialog() }
                        )
                    }
                    is StatusDialogState.QuestChecklist -> {
                        val quest  = if (dialog.isDaily) data.dailyQuest else data.mainQuest
                        val accent = if (dialog.isDaily) NeonCyan else NeonGold
                        quest?.let { q ->
                            QuestChecklistSheet(
                                quest        = q,
                                accentColor  = accent,
                                onTaskToggle = { task ->
                                    viewModel.onTaskToggled(task, dialog.questId)
                                },
                                onDismiss    = { viewModel.onDismissDialog() }
                            )
                        }
                    }
                    is StatusDialogState.EditSystemConfig -> {
                        // Тимчасова заглушка для уникнення крашу
                        viewModel.onDismissDialog()
                    }
                    StatusDialogState.None -> Unit
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

    // ── Global One-off Dialogs ────────────────────────────────────────
    levelUpEvent?.let { event ->
        LevelUpDialog(
            newClass  = event.newClass,
            newMonth  = event.newMonth,
            onDismiss = { levelUpEvent = null }
        )
    }
    if (showPenaltyOn) {
        PenaltyActivatedDialog(onDismiss = { showPenaltyOn = false })
    }
    if (showPenaltyOff) {
        PenaltyDeactivatedDialog(onDismiss = { showPenaltyOff = false })
    }
}