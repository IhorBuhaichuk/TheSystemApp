package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemIconButton
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemPlateShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import com.ihor.thesystem.presentation.common.components.ExerciseAnimationPlayer
import com.ihor.thesystem.presentation.common.components.HologramExerciseImage
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

@Composable
internal fun ExerciseInfoDialog(
    exercise: ExerciseWorkoutUiModel,
    matrixEntry: MatrixEntryUiModel?,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SystemDialogContainer(
            modifier = Modifier
                .fillMaxWidth(0.92f),
            accent = colors.accentAi,
            contentPadding = SystemCardPadding
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.nameUk ?: exercise.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = exercise.recommendation ?: exercise.trackingMode.inputSummary(exercise.sets.size),
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SystemIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Закрити",
                    onClick = onDismiss,
                    accent = colors.accentAi
                )
            }

            if (exercise.externalId != null) {
                ExerciseAnimationPlayer(
                    exerciseExternalId = exercise.externalId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .techSurface(
                            shape = systemPlateShape(),
                            active = false,
                            accent = colors.accentAi,
                            role = TechSurfaceRole.Plate
                        )
                        .clip(systemPlateShape())
                )
            } else if (exercise.gifUrl != null) {
                HologramExerciseImage(
                    gifUrl = exercise.gifUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .techSurface(
                            shape = systemPlateShape(),
                            active = false,
                            accent = colors.accentAi,
                            role = TechSurfaceRole.Plate
                        )
                        .clip(systemPlateShape())
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .techSurface(
                            shape = systemPlateShape(),
                            active = false,
                            accent = colors.accentAi,
                            role = TechSurfaceRole.Plate
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Анімація для цієї вправи недоступна",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                    )
                }
            }

            Text(
                text = "Довідка винесена окремо, щоб під час тренування основний екран лишався швидким журналом підходів.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
            )

            matrixEntry?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Поточна", value = it.displayCurrent)
                    StatItem(label = "Ціль", value = it.displayTarget)
                    StatItem(label = "Ранг", value = it.currentRank.name)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colors = SystemTheme.colors
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
