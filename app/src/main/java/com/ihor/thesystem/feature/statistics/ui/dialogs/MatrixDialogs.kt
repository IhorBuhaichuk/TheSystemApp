package com.ihor.thesystem.feature.statistics.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput

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
        Column(
            modifier = Modifier
                .sciPanel(NeonGold.copy(0.5f), BackgroundDeep, 16.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "[ НАЛАШТУВАННЯ МАТРИЦІ ]",
                color = NeonGold,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = exerciseName.uppercase(),
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            NeonInputField(label = "Стартова вага (кг)", value = start) { start = it }
            NeonInputField(label = "Цільова вага (кг)", value = target) { target = it }

            Button(
                onClick = { onConfirm(start, target) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGold.copy(0.2f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "[ ПІДТВЕРДИТИ ]",
                    color = NeonGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LogWorkoutSetsDialog(
    exerciseName: String,
    sets: List<WorkoutSetInput>,
    onUpdate: (Long, String, String) -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sciPanel(NeonCyan.copy(0.5f), BackgroundDeep, 16.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "[ ЛОГУВАННЯ ПІДХОДІВ ]",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = exerciseName.uppercase(),
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sets, key = { it.id }) { set ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "СЕТ ${sets.indexOf(set) + 1}",
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                modifier = Modifier.width(45.dp)
                            )
                            SmallNeonInputField(
                                hint = "Вага",
                                value = set.weight,
                                modifier = Modifier.weight(1f)
                            ) { onUpdate(set.id, it, set.reps) }
                            
                            SmallNeonInputField(
                                hint = "Повт",
                                value = set.reps,
                                modifier = Modifier.weight(1f)
                            ) { onUpdate(set.id, set.weight, it) }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(0.3f))
                ) {
                    Text("- СЕТ", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onAdd,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(0.3f))
                ) {
                    Text("+ СЕТ", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "[ ЗАЛОГУВАТИ ]",
                    color = BackgroundDeep,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NeonInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PanelSurface,
            unfocusedContainerColor = PanelSurface,
            focusedTextColor = NeonCyan,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeonCyan,
            focusedIndicatorColor = NeonCyan,
            unfocusedIndicatorColor = TextSecondary.copy(0.3f)
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
    )
}

@Composable
private fun SmallNeonInputField(hint: String, value: String, modifier: Modifier, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, color = TextSecondary.copy(0.5f), fontSize = 10.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TextPrimary
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PanelSurface,
            unfocusedContainerColor = PanelSurface,
            focusedIndicatorColor = NeonCyan,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = NeonCyan
        ),
        maxLines = 1
    )
}
