package com.ihor.thesystem.feature.architect.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.AccentAi
import com.ihor.thesystem.core.theme.AccentAiSoft
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemSurfaceGlass
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.domain.model.AnnualProgressionAdjustment
import com.ihor.thesystem.domain.model.AnnualProgressionExercisePlan
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyTarget
import com.ihor.thesystem.domain.model.AnnualProgressionPlan
import com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionExerciseUiModel
import com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionPlanUiState
import com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionPlanViewModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AnnualProgressionPlanScreen(
    onBack: () -> Unit,
    onOpenExercisePicker: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnnualProgressionPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        Toast.makeText(context, message.asString(context), Toast.LENGTH_SHORT).show()
        viewModel.onMessageShown()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            AnnualPlanHeader(onBack = onBack)
            AdaptationStatusBlock(
                state = uiState,
                onUseToday = viewModel::onUseTodayAsStartDate,
                onSelectDate = viewModel::onStartDateSelected
            )
            SelectedExercisesBlock(
                state = uiState,
                onOpenExercisePicker = onOpenExercisePicker,
                onRemoveExercise = viewModel::onRemoveExercise,
                onToggleExpanded = viewModel::onExerciseExpanded,
                onTargetWeightChanged = viewModel::onTargetWeightChanged,
                onInventoryStepChanged = viewModel::onInventoryStepChanged
            )
            GenerationBlock(
                state = uiState,
                onGeneratePlan = viewModel::onGeneratePlan
            )
            uiState.generatedPlan?.let { plan ->
                AnnualPlanResultBlock(
                    plan = plan,
                    expandedResultIds = uiState.expandedResultIds,
                    isSaving = uiState.isSaving,
                    onToggleExpanded = viewModel::onResultExpanded,
                    onSavePlan = viewModel::onSavePlan
                )
            }
        }
    }
}

@Composable
private fun AnnualPlanHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, BorderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "РќР°Р·Р°Рґ",
                tint = TextSecondary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "РџР»Р°РЅ СЂС–С‡РЅРѕС— РїСЂРѕРіСЂРµСЃС–С—",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "12 РјС–СЃСЏС†С–РІ РїРѕ РІРёР±СЂР°РЅРёС… РІРїСЂР°РІР°С…",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdaptationStatusBlock(
    state: AnnualProgressionPlanUiState,
    onUseToday: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    DarkGlassCard(active = state.isAdaptationComplete) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РЎС‚Р°С‚СѓСЃ Р°РґР°РїС‚Р°С†С–С—",
                subtitle = "2 С‚РёР¶РЅС– РїРµСЂРµРґ С„С–РЅР°Р»СЊРЅРёРј РїР»Р°РЅРѕРј"
            )
            Text(
                text = "Р‘Р°Р¶Р°С”С‚Рµ СЂРѕР·РїРѕС‡Р°С‚Рё РІР°С€ С‡РµР»РµРЅРґР¶ СЃСЊРѕРіРѕРґРЅС– С‡Рё РІРёР±СЂР°С‚Рё РїРµРІРЅСѓ РґР°С‚Сѓ РїРѕС‡Р°С‚РєСѓ?",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "РЎСЊРѕРіРѕРґРЅС–",
                    onClick = onUseToday,
                    accent = AccentAi,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Р’РёР±СЂР°С‚Рё РґР°С‚Сѓ",
                    onClick = { showDatePicker = true },
                    accent = AccentAi,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SystemStatusChip(
                    text = "РЎС‚Р°СЂС‚: ${state.startDate.formatDate()}",
                    accent = AccentAi,
                    active = true
                )
                SystemStatusChip(
                    text = "РџС–СЃР»СЏ: ${state.adaptationEndsOn.formatDate()}",
                    accent = AccentPrimary,
                    active = state.isAdaptationComplete
                )
            }
            Text(
                text = if (state.isAdaptationComplete) {
                    "РђРґР°РїС‚Р°С†С–СЋ Р·Р°РІРµСЂС€РµРЅРѕ. РњРѕР¶РЅР° С„РѕСЂРјСѓРІР°С‚Рё С„С–РЅР°Р»СЊРЅРёР№ СЂС–С‡РЅРёР№ РїР»Р°РЅ."
                } else {
                    "Р”Рѕ Р·Р°РІРµСЂС€РµРЅРЅСЏ Р°РґР°РїС‚Р°С†С–С—: ${state.adaptationRemainingDays} РґРЅ. Р¤С–РЅР°Р»СЊРЅРёР№ РїР»Р°РЅ РЅРµ РіРµРЅРµСЂСѓС”С‚СЊСЃСЏ Р·Р°РІС‡Р°СЃРЅРѕ."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (state.isAdaptationComplete) AccentAi else TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

    if (showDatePicker) {
        AnnualStartDatePickerDialog(
            initialDate = state.startDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                onSelectDate(date)
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnualStartDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toLocalDate()
                        ?.let(onConfirm)
                }
            ) {
                Text(text = "РћР±СЂР°С‚Рё", color = AccentAi)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "РЎРєР°СЃСѓРІР°С‚Рё", color = TextSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun SelectedExercisesBlock(
    state: AnnualProgressionPlanUiState,
    onOpenExercisePicker: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onToggleExpanded: (Int) -> Unit,
    onTargetWeightChanged: (Int, String) -> Unit,
    onInventoryStepChanged: (Int, String) -> Unit
) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р’РёР±СЂР°РЅС– РІРїСЂР°РІРё",
                subtitle = if (state.selectedExercises.isEmpty()) {
                    "Р”РѕРґР°Р№ РІРїСЂР°РІРё РґР»СЏ РїСЂРѕРіРЅРѕР·Сѓ"
                } else {
                    "${state.selectedExercises.size} РІРїСЂР°РІ Сѓ РїР»Р°РЅС–"
                },
                trailing = {
                    SystemButton(
                        text = "Р”РѕРґР°С‚Рё",
                        icon = Icons.Filled.Add,
                        onClick = onOpenExercisePicker,
                        accent = AccentAi
                    )
                }
            )
            if (state.selectedExercises.isEmpty()) {
                EmptyAnnualBlock(text = "РЎРїРёСЃРѕРє РїРѕСЂРѕР¶РЅС–Р№. РћР±РµСЂРё РІРїСЂР°РІРё Р· Р±Р°Р·Рё, С‰РѕР± РїС–РґРіРѕС‚СѓРІР°С‚Рё СЂС–С‡РЅРёР№ РїСЂРѕРіРЅРѕР·.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.selectedExercises.forEach { exercise ->
                        SelectedExerciseCard(
                            exercise = exercise,
                            onRemove = { onRemoveExercise(exercise.exerciseId) },
                            onToggleExpanded = { onToggleExpanded(exercise.exerciseId) },
                            onTargetWeightChanged = { value ->
                                onTargetWeightChanged(exercise.exerciseId, value)
                            },
                            onInventoryStepChanged = { value ->
                                onInventoryStepChanged(exercise.exerciseId, value)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedExerciseCard(
    exercise: AnnualProgressionExerciseUiModel,
    onRemove: () -> Unit,
    onToggleExpanded: () -> Unit,
    onTargetWeightChanged: (String) -> Unit,
    onInventoryStepChanged: (String) -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, BorderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "РџРѕС‚РѕС‡РЅР°: ${exercise.currentWorkingWeight.formatNullableWeight()} В· РџРѕРІС‚.: ${exercise.reps ?: "вЂ”"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleExpanded, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = if (exercise.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AccentAi
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Р’РёРґР°Р»РёС‚Рё",
                    tint = TextMuted
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnnualNumberField(
                value = exercise.targetWeightInput,
                onValueChange = onTargetWeightChanged,
                label = "Р¦С–Р»СЊ Рњ12",
                modifier = Modifier.weight(1f)
            )
            AnnualNumberField(
                value = exercise.inventoryStepInput,
                onValueChange = onInventoryStepChanged,
                label = "РљСЂРѕРє",
                modifier = Modifier.weight(1f)
            )
        }

        if (exercise.isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AnnualMetricLine(label = "РћСЃС‚Р°РЅРЅС” С‚СЂРµРЅСѓРІР°РЅРЅСЏ", value = exercise.lastTrainingTimestamp.formatTimestamp())
                AnnualMetricLine(label = "Estimated 1RM", value = exercise.estimatedOneRepMax.formatNullableWeight())
                if (!exercise.canGenerate) {
                    Text(
                        text = "РџРѕС‚СЂС–Р±РЅС– РїРѕС‚РѕС‡РЅР° РІР°РіР°, С†С–Р»СЊ РІРёС‰Рµ РїРѕС‚РѕС‡РЅРѕС— С‚Р° РєСЂРѕРє С–РЅРІРµРЅС‚Р°СЂСЋ.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnualNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label, color = TextMuted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentAi,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = SystemSurfaceGlass.copy(alpha = 0.42f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.018f),
            cursorColor = AccentAi,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun AnnualMetricLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GenerationBlock(
    state: AnnualProgressionPlanUiState,
    onGeneratePlan: () -> Unit
) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р“РµРЅРµСЂР°С†С–СЏ",
                subtitle = "Step-loading В· 12 РјС–СЃСЏС†С–РІ В· РїР»Р°С‚Рѕ С– СЃС‚СЂРёР±РєРё"
            )
            Text(
                text = when {
                    !state.isAdaptationComplete -> "РџР»Р°РЅ Р±СѓРґРµ РґРѕСЃС‚СѓРїРЅРёР№ РїС–СЃР»СЏ 2 С‚РёР¶РЅС–РІ Р°РґР°РїС‚Р°С†С–С—."
                    state.selectedExercises.isEmpty() -> "Р”РѕРґР°Р№ С…РѕС‡Р° Р± РѕРґРЅСѓ РІРїСЂР°РІСѓ."
                    !state.canGenerate -> "Р—Р°РїРѕРІРЅРё РїРѕРєР°Р·РЅРёРєРё РґР»СЏ РєРѕР¶РЅРѕС— РІРїСЂР°РІРё."
                    else -> "Р”Р°РЅС– РіРѕС‚РѕРІС– РґРѕ С„РѕСЂРјСѓРІР°РЅРЅСЏ РїР»Р°РЅСѓ."
                },
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
            SystemButton(
                text = if (state.isGenerating) "Р¤РѕСЂРјСѓСЋ..." else "РЎС„РѕСЂРјСѓРІР°С‚Рё РїР»Р°РЅ",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onGeneratePlan,
                accent = AccentAi,
                enabled = !state.isGenerating && state.canGenerate,
                glow = state.canGenerate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnnualPlanResultBlock(
    plan: AnnualProgressionPlan,
    expandedResultIds: Set<Int>,
    isSaving: Boolean,
    onToggleExpanded: (Int) -> Unit,
    onSavePlan: () -> Unit
) {
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р РµР·СѓР»СЊС‚Р°С‚",
                subtitle = "РџР»Р°РЅ СЃС‚Р°СЂС‚СѓС” ${plan.startDate.formatDate()}"
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plan.exercises.forEach { exercisePlan ->
                    AnnualExercisePlanCard(
                        plan = exercisePlan,
                        expanded = exercisePlan.exerciseId in expandedResultIds,
                        onToggleExpanded = { onToggleExpanded(exercisePlan.exerciseId) }
                    )
                }
            }
            SystemButton(
                text = if (isSaving) "Р—Р±РµСЂС–РіР°СЋ..." else "Р—Р±РµСЂРµРіС‚Рё РїР»Р°РЅ",
                icon = Icons.Filled.Save,
                onClick = onSavePlan,
                accent = AccentAi,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnnualExercisePlanCard(
    plan: AnnualProgressionExercisePlan,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AccentAiSoft.copy(alpha = 0.34f))
            .border(1.dp, AccentAi.copy(alpha = 0.18f), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = plan.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${plan.currentWeight.formatWeight()} РєРі в†’ ${plan.targetWeight.formatWeight()} РєРі",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AccentAi,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            SystemStatusChip(text = "Р—Р° РїР»Р°РЅРѕРј", accent = AccentAi, active = true)
            IconButton(onClick = onToggleExpanded, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AccentAi
                )
            }
        }
        if (expanded) {
            AnnualMetricLine(label = "Р†РґРµР°Р»СЊРЅРёР№ РєСЂРѕРє", value = "${plan.idealMonthlyStep.formatWeight()} РєРі/РјС–СЃ")
            AnnualMetricLine(label = "РљСЂРѕРє С–РЅРІРµРЅС‚Р°СЂСЋ", value = "${plan.inventoryStep.formatWeight()} РєРі")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(plan.monthlyTargets, key = { it.monthIndex }) { target ->
                    MonthlyTargetChip(target = target)
                }
            }
        }
    }
}

@Composable
private fun MonthlyTargetChip(target: AnnualProgressionMonthlyTarget) {
    val accent = when (target.adjustment) {
        AnnualProgressionAdjustment.Baseline -> TextMuted
        AnnualProgressionAdjustment.StandardStep -> AccentAi
        AnnualProgressionAdjustment.Plateau -> AccentPrimary
        AnnualProgressionAdjustment.ForcedJump -> AccentAi
    }
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.24f), RoundedCornerShape(13.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "M${target.monthIndex}",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = target.weight.formatWeight(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
        )
        if (target.adjustment == AnnualProgressionAdjustment.Plateau) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EmptyAnnualBlock(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.026f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp),
        style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
    )
}

private fun LocalDate.formatDate(): String =
    format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("uk")))

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun Long?.formatTimestamp(): String =
    this?.toLocalDate()?.formatDate() ?: "вЂ”"

private fun Double?.formatNullableWeight(): String =
    this?.let { "${it.formatWeight()} РєРі" } ?: "вЂ”"

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
