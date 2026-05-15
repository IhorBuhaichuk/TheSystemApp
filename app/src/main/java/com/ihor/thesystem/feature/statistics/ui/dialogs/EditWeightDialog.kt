package com.ihor.thesystem.feature.statistics.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import java.util.Locale

@Composable
fun EditWeightDialog(
    entry: MatrixEntryUiModel,
    onConfirm: (Float, String) -> Unit,
    onDismiss: () -> Unit,
    existingLog: ExerciseSet? = null
) {
    val colors = SystemTheme.colors
    val usesExternalLoad = entry.usesExternalLoad
    var input by remember(entry.exerciseId, usesExternalLoad) {
        mutableStateOf(if (usesExternalLoad) entry.currentWeight.toString() else "")
    }
    var feedback by remember(existingLog) { mutableStateOf(existingLog?.userFeedback ?: "") }
    var isError by remember { mutableStateOf(false) }
    val canConfirm = !usesExternalLoad || (!isError && input.isNotBlank())

    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(accent = colors.accentSuccess) {
            Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = colors.accentSuccess)
            Text(
                text = if (usesExternalLoad) "[ ПОТОЧНА ВАГА ]" else "[ ВПРАВА БЕЗ КГ ]",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentSuccess,
                    fontWeight = FontWeight.Bold
                )
            )
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                Text(
                    text = entry.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (usesExternalLoad) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
                    ) {
                        InfoChip("СТАРТ", entry.displayStart, Modifier.weight(1f))
                        InfoChip("ЦІЛЬ", entry.displayTarget, Modifier.weight(1f))
                        InfoChip("+/тиж", "+${String.format(Locale.US, "%.2f", entry.weeklyStep)}кг", Modifier.weight(1f))
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            isError = it.toFloatOrNull() == null && it.isNotEmpty()
                        },
                        singleLine = true,
                        isError = isError,
                        label = { Text("Поточна вага (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = systemOutlinedTextFieldColors(accent = colors.accentSuccess),
                        textStyle = LocalTextStyle.current.copy(color = colors.textPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isError) {
                        Text(
                            text = "Введіть коректне число",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.accentError)
                        )
                    }

                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Фітбек (відчуття, біль, легкість...)") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary),
                        textStyle = LocalTextStyle.current.copy(color = colors.textPrimary)
                    )
                } else {
                    Text(
                        text = "Для цієї вправи поточна вага не редагується. Використовуй логування підходів, щоб фіксувати повторення або час.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                SystemGhostButton(
                    text = "СКАСУВАТИ",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = if (usesExternalLoad) "ЗБЕРЕГТИ" else "ЗАКРИТИ",
                    onClick = {
                        if (usesExternalLoad) {
                            input.toFloatOrNull()?.let { onConfirm(it, feedback) }
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    accent = colors.accentSuccess,
                    enabled = !usesExternalLoad || (!isError && input.isNotBlank()),
                    glow = canConfirm
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemCardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.accentPrimary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
