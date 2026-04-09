package com.ihor.thesystem.feature.status.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.DomainQuestType

// --- Імпорти з пакета statistics ---
import com.ihor.thesystem.feature.statistics.ui.components.EmptyQuestCard
import com.ihor.thesystem.feature.statistics.ui.components.PlayerLeftPanel
import com.ihor.thesystem.feature.statistics.ui.components.QuestCard
import com.ihor.thesystem.feature.statistics.ui.components.StatRightPanel
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.*

// --- ВИПРАВЛЕНИЙ ІМПОРТ: SystemHeader має пакет status всередині файлу ---
import com.ihor.thesystem.feature.status.ui.components.SystemHeader
import com.ihor.thesystem.feature.status.ui.components.CurrentDateBlock

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
    val context = LocalContext.current
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
                    Column(
                        modifier = Modifier.weight(0.60f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlayerLeftPanel(
                            data         = data,
                            modifier     = Modifier.fillMaxWidth(),
                            onNameTap    = { viewModel.onNameTap() },
                            onDebuffEdit = { viewModel.onDebuffTap() }
                        )
                        CurrentDateBlock(modifier = Modifier.fillMaxWidth())
                    }

                    StatRightPanel(
                        month       = "${data.currentMonth}/${data.totalMonths}",
                        weight      = "${data.currentWeight.toInt()}",
                        height      = "${data.height.toInt()}",
                        modifier    = Modifier.weight(0.40f),
                        onWeightTap = { viewModel.onWeightTap() },
                        onHeightTap = { viewModel.onHeightTap() }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. РУТИНА (DAILY)
                    data.dailyQuest?.let { quest ->
                        QuestCard(
                            quest       = quest,
                            type        = DomainQuestType.DAILY,
                            onClick     = { viewModel.onQuestTap(quest.id, isDaily = true) },
                            onLongClick = { /* TODO: редактор квесту */ }
                        )
                    } ?: EmptyQuestCard(DomainQuestType.DAILY)

                    // 2. ОСНОВНИЙ КВЕСТ (MAIN)
                    data.mainQuest?.let { quest ->
                        QuestCard(
                            quest       = quest,
                            type        = DomainQuestType.MAIN,
                            onClick     = { viewModel.onQuestTap(quest.id, isDaily = false) },
                            onLongClick = { /* TODO: редактор квесту */ }
                        )
                    } ?: EmptyQuestCard(DomainQuestType.MAIN)

                    // 3. ЕКЗАМЕНИ (PROMOTION)
                    data.promotionQuests.forEach { quest ->
                        QuestCard(
                            quest       = quest,
                            type        = DomainQuestType.PROMOTION,
                            onClick     = { viewModel.onQuestTap(quest.id, isDaily = false) },
                            onLongClick = { /* TODO: редактор квесту */ }
                        )
                    }

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
                    is StatusDialogState.EditHeight -> {
                        LogHeightDialog(
                            currentHeight = data.height,
                            onConfirm     = { viewModel.onHeightConfirmed(it) },
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
                        val quest = if (dialog.isDaily) {
                            data.dailyQuest
                        } else {
                            data.mainQuest ?: data.promotionQuests.find { it.id == dialog.questId }
                        }
                        
                        val accent = if (dialog.isDaily) NeonCyan else NeonGold

                        quest?.let { q ->
                            QuestChecklistSheet(
                                quest        = q,
                                accentColor  = accent,
                                onTaskToggle = { task ->
                                    viewModel.onTaskToggled(task, dialog.questId)
                                },
                                onAddTask    = { taskName ->
                                    viewModel.onAddTask(dialog.questId, taskName)
                                },
                                onRemoveTask = { taskId ->
                                    viewModel.onRemoveTask(taskId)
                                },
                                onDismiss    = { viewModel.onDismissDialog() }
                            )
                        }
                    }
                    is StatusDialogState.EditSystemConfig -> {
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
                        text       = "[ ПОМИЛКА: ${state.message} ]",
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
