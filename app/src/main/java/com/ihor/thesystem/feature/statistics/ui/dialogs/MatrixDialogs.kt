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
            title = "НАЛАШТУВАННЯ МАТРИЦІ",
            accentColor = NeonGold,
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
                
                PremiumInputField(label = "Стартова вага (кг)", value = start, accentColor = NeonGold) { start = it }
                PremiumInputField(label = "Цільова вага (кг)", value = target, accentColor = NeonGold) { target = it }

                Spacer(modifier = Modifier.height(8.dp))

                PremiumDialogButton(
                    text = "ПІДТВЕРДИТИ",
                    color = NeonGold,
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
                            
                            SmallPremiumInputField(
                                hint = "Вага",
                                value = set.weight,
                                accentColor = NeonCyan,
                                modifier = Modifier.weight(1f)
                            ) { onUpdate(set.id, it, set.reps) }
                            
                            SmallPremiumInputField(
                                hint = "Повт",
                                value = set.reps,
                                accentColor = NeonCyan,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF020408))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
    ) {
        // Decorative glow
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .background(accentColor.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.3f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
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
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f))
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = accentColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun SmallPremiumInputField(
    hint: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, fontSize = 12.sp, color = Color.White.copy(alpha = 0.2f)) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.Black
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
    }
}

