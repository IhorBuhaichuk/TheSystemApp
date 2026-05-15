package com.ihor.thesystem.feature.statistics.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode

@Composable
fun SetupMatrixDialog(
    exerciseName: String,
    initialStart: String,
    initialTarget: String,
    usesExternalLoad: Boolean = true,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    var start by remember(initialStart, usesExternalLoad) {
        mutableStateOf(if (usesExternalLoad) initialStart else "")
    }
    var target by remember(initialTarget, usesExternalLoad) {
        mutableStateOf(if (usesExternalLoad) initialTarget else "")
    }

    Dialog(onDismissRequest = onDismiss) {
        PremiumDialogContainer(
            title = "Налаштування матриці",
            accentColor = colors.accentPrimary,
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                
                if (usesExternalLoad) {
                    PremiumInputField(label = "Стартова вага (кг)", value = start, accentColor = colors.accentPrimary) { start = it }
                    PremiumInputField(label = "Цільова вага (кг)", value = target, accentColor = colors.accentPrimary) { target = it }
                } else {
                    Text(
                        text = "Ця вправа не використовує зовнішню вагу. Прогрес фіксується через повторення або час у логуванні підходів.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PremiumDialogButton(
                    text = if (usesExternalLoad) "Підтвердити" else "Зрозуміло",
                    color = colors.accentPrimary,
                    onClick = {
                        if (usesExternalLoad) {
                            onConfirm(start, target)
                        } else {
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LogWorkoutSetsDialog(
    exerciseName: String,
    sets: List<ActiveSetInput>,
    trackingMode: ExerciseTrackingMode = ExerciseTrackingMode.WEIGHT_REPS,
    onUpdate: (Long, String, String) -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    existingLogs: List<ExerciseSet> = emptyList()
) {
    val colors = SystemTheme.colors
    var feedback by remember(existingLogs) { mutableStateOf(existingLogs.firstOrNull()?.userFeedback ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        PremiumDialogContainer(
            title = "ЛОГУВАННЯ ПІДХОДІВ",
            accentColor = colors.accentPrimary,
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = exerciseName.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                // Введення підходів
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sets.forEachIndexed { index, set ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "СЕТ ${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.textMuted,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.width(40.dp)
                            )
                            
                            if (trackingMode.usesWeightInput) {
                                SmallPremiumInputField(
                                    hint = "Вага",
                                    value = set.weight,
                                    accentColor = colors.accentPrimary,
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f)
                                ) { onUpdate(set.id, it, set.reps) }
                            }
                            
                            SmallPremiumInputField(
                                hint = trackingMode.valueLabel,
                                value = set.reps,
                                accentColor = colors.accentPrimary,
                                keyboardType = if (trackingMode.usesTimeInput) KeyboardType.Text else KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            ) { onUpdate(set.id, set.weight, it) }
                        }
                    }
                }

                // Керування підходами
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(colors.overlayLight, RoundedCornerShape(SystemTheme.shapes.small))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = colors.accentError)
                    }
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(colors.overlayLight, RoundedCornerShape(SystemTheme.shapes.small))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.accentSuccess)
                    }
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    placeholder = { Text("Фітбек (відчуття, памп...)", color = colors.textMuted) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SystemTheme.shapes.medium),
                    colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary)
                )

                PremiumDialogButton(
                    text = "ЗАЛОГУВАТИ",
                    color = colors.accentPrimary,
                    onClick = { onSave(feedback) }
                )
            }
        }
    }
}

@Composable
fun PremiumDialogContainer(
    title: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = SystemTheme.colors
    SystemDialogContainer(
        modifier = Modifier.fillMaxWidth(0.92f),
        accent = accentColor,
        contentPadding = SystemCardPadding
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(SystemCardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(SystemTheme.shapes.small))
                        .background(colors.overlayLight)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(SystemTheme.shapes.small))
                ) {
                    Icon(Icons.Default.Close, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                }
            }
            content()
        }
    }
}

@Composable
private fun PremiumInputField(
    label: String,
    value: String,
    accentColor: Color,
    onValueChange: (String) -> Unit
) {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(SystemTheme.shapes.medium),
            colors = systemOutlinedTextFieldColors(accent = accentColor),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true
        )
    }
}

@Composable
private fun SmallPremiumInputField(
    hint: String,
    value: String,
    accentColor: Color,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val colors = SystemTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
            )
        },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(SystemTheme.shapes.small),
        colors = systemOutlinedTextFieldColors(accent = accentColor),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        ),
        singleLine = true
    )
}

@Composable
private fun PremiumDialogButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    SystemButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        accent = color,
        glow = true
    )
}

