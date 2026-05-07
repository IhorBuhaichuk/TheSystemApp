package com.ihor.thesystem.feature.statistics.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode

@Composable
fun SetupMatrixDialog(
    exerciseName: String,
    initialStart: String,
    initialTarget: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var start by remember { mutableStateOf(initialStart) }
    var target by remember { mutableStateOf(initialTarget) }

    Dialog(onDismissRequest = onDismiss) {
        PremiumDialogContainer(
            title = "Налаштування матриці",
            accentColor = AccentPrimary,
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                
                PremiumInputField(label = "Стартова вага (кг)", value = start, accentColor = AccentPrimary) { start = it }
                PremiumInputField(label = "Цільова вага (кг)", value = target, accentColor = AccentPrimary) { target = it }

                Spacer(modifier = Modifier.height(8.dp))

                PremiumDialogButton(
                    text = "Підтвердити",
                    color = AccentPrimary,
                    onClick = { onConfirm(start, target) }
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
    var feedback by remember(existingLogs) { mutableStateOf(existingLogs.firstOrNull()?.userFeedback ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        PremiumDialogContainer(
            title = "ЛОГУВАННЯ ПІДХОДІВ",
            accentColor = NeonCyan,
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = exerciseName.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.width(40.dp)
                            )
                            
                            if (trackingMode.usesWeightInput) {
                                SmallPremiumInputField(
                                    hint = "Вага",
                                    value = set.weight,
                                    accentColor = NeonCyan,
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f)
                                ) { onUpdate(set.id, it, set.reps) }
                            }
                            
                            SmallPremiumInputField(
                                hint = trackingMode.valueLabel,
                                value = set.reps,
                                accentColor = NeonCyan,
                                keyboardType = if (trackingMode.usesTimeInput) KeyboardType.Text else KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            ) { onUpdate(set.id, set.weight, it) }
                        }
                    }
                }

                // Керування підходами
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = NeonRed)
                    }
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = NeonGreen)
                    }
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    placeholder = { Text("Фітбек (відчуття, памп...)", color = Color.White.copy(alpha = 0.2f)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                PremiumDialogButton(
                    text = "ЗАЛОГУВАТИ",
                    color = NeonCyan,
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
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SystemSurfaceGlassStrong,
                        SystemSurfaceGlass,
                        SystemSurfaceGlass.copy(alpha = 0.64f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.14f),
                        accentColor.copy(alpha = 0.24f),
                        BorderSubtle
                    )
                ),
                shape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.035f))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                cursorColor = accentColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = TextPrimary,
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, fontSize = 12.sp, color = Color.White.copy(alpha = 0.2f)) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            cursorColor = accentColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
        singleLine = true
    )
}

@Composable
private fun PremiumDialogButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.045f)
                    )
                )
            )
            .border(1.dp, color.copy(alpha = 0.52f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

