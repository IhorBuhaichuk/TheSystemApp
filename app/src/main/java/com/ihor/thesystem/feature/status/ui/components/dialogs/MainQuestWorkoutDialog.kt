package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemIconButton
import com.ihor.thesystem.feature.status.ui.components.workout.ActiveDayCard
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

@Composable
fun MainQuestWorkoutDialog(
    data: ActiveDayUiModel?,
    onSetWeightChanged: (Int, Long, String) -> Unit,
    onSetRepsChanged: (Int, Long, String) -> Unit,
    onSetFocusLost: (Int, Long) -> Unit,
    onSetCompletionChanged: (Int, Long, Boolean) -> Unit,
    onAddSet: (Int, String) -> Unit,
    onOpenSetup: (MatrixEntryUiModel) -> Unit,
    onFinishWorkout: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Тренування",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    SystemIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = onDismiss
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (data != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            ActiveDayCard(
                                data = data,
                                onSetWeightChanged = onSetWeightChanged,
                                onSetRepsChanged = onSetRepsChanged,
                                onSetFocusLost = onSetFocusLost,
                                onSetCompletionChanged = onSetCompletionChanged,
                                onAddSet = onAddSet,
                                onOpenSetup = onOpenSetup
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            SystemButton(
                                text = "Завершити тренування",
                                onClick = onFinishWorkout,
                                glow = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.accentPrimary)
                        }
                    }
                }
            }
        }
    }
}
