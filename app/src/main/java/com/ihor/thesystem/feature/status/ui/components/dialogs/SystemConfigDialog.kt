package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.SystemConfig

@Composable
fun SystemConfigDialog(
    config: SystemConfig,
    onConfirm: (SystemConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var penalty by remember { mutableStateOf(config.defaultPenalty.toString()) }
    var sets by remember { mutableStateOf(config.targetSets.toString()) }
    var reps by remember { mutableStateOf(config.targetReps.toString()) }
    var weeks by remember { mutableStateOf(config.matrixWeeks.toString()) }

    val isValid = listOf(penalty, sets, reps, weeks).all { it.toIntOrNull() != null }
    val scrollState = rememberScrollState()
    val colors = SystemTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(accent = colors.accentPrimary) {
            Icon(Icons.Filled.Settings, contentDescription = null, tint = colors.accentPrimary)
            Text(
                text = "Система: конфіг",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Black
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                ConfigField(
                    label = "Штраф (%)",
                    value = penalty,
                    onChange = { penalty = it }
                )
                ConfigField(
                    label = "Цільові підходи",
                    value = sets,
                    onChange = { sets = it }
                )
                ConfigField(
                    label = "Цільові повтори",
                    value = reps,
                    onChange = { reps = it }
                )
                ConfigField(
                    label = "Тижнів у плані",
                    value = weeks,
                    onChange = { weeks = it }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                SystemGhostButton(
                    text = "Скасувати",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Зберегти",
                    onClick = {
                        onConfirm(
                            config.copy(
                                defaultPenalty = penalty.toIntOrNull() ?: config.defaultPenalty,
                                targetSets = sets.toIntOrNull() ?: config.targetSets,
                                targetReps = reps.toIntOrNull() ?: config.targetReps,
                                matrixWeeks = weeks.toIntOrNull() ?: config.matrixWeeks
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    accent = colors.accentPrimary,
                    enabled = isValid,
                    glow = isValid
                )
            }
        }
    }
}

@Composable
private fun ConfigField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    val colors = SystemTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = systemOutlinedTextFieldColors(colors.accentPrimary),
        shape = RoundedCornerShape(SystemTheme.shapes.medium),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
