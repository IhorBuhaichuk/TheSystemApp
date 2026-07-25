package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemIconButton
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.feature.status.ui.components.workout.ActiveDayCard
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutLoggingSummaryUiModel
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
                        val summary = data.loggingSummary
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            WorkoutLoggingSummaryPanel(summary = summary)

                            Spacer(modifier = Modifier.height(14.dp))

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
                                text = summary.finishCtaText,
                                onClick = onFinishWorkout,
                                enabled = summary.canFinish,
                                glow = summary.canFinish,
                                icon = Icons.Filled.CheckCircle,
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

@Composable
private fun WorkoutLoggingSummaryPanel(
    summary: WorkoutLoggingSummaryUiModel,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.large)
    val accent = if (summary.canFinish) colors.accentSuccess else colors.accentPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, accent.copy(alpha = if (summary.canFinish) 0.34f else 0.18f), shape)
            .padding(SystemCardPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = summary.progressText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SystemStatusChip(
                text = if (summary.canFinish) "READY" else "LOG SET",
                accent = accent,
                active = summary.canFinish
            )
        }

        Text(
            text = "${summary.exerciseText} · ${summary.helperText}",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
