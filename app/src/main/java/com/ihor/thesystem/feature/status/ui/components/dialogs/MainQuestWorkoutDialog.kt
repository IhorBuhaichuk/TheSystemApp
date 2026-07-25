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
import com.ihor.thesystem.core.ui.components.SystemDialogScaffold
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemPlateShape
import com.ihor.thesystem.core.ui.components.techSurface
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
        val summary = data?.loggingSummary
        SystemDialogScaffold(
            title = "Тренування",
            onDismiss = onDismiss,
            accent = colors.accentPrimary,
            bottomBar = summary?.let {
                {
                    SystemButton(
                        text = it.finishCtaText,
                        onClick = onFinishWorkout,
                        enabled = it.canFinish,
                        glow = it.canFinish,
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        ) {
            if (data != null) {
                Column(
                    modifier = Modifier
                    .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    WorkoutLoggingSummaryPanel(summary = data.loggingSummary)

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
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accentPrimary)
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
    val accent = if (summary.canFinish) colors.accentSuccess else colors.accentPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .techSurface(
                shape = systemPlateShape(),
                active = summary.canFinish,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
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
                text = if (summary.canFinish) "Можна завершити" else "Запишіть підхід",
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
