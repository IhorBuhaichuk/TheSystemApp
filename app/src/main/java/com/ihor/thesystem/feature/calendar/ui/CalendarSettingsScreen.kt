package com.ihor.thesystem.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentPrimarySoft
import com.ihor.thesystem.core.theme.AccentWarning
import com.ihor.thesystem.core.theme.BorderActive
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemSurfaceGlass
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarCycleTemplate
import com.ihor.thesystem.domain.model.title
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarCycleDayDraftUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarSettingsEvent
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarSettingsUiState
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarSettingsViewModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop

@Composable
fun CalendarSettingsScreen(
    onBack: () -> Unit,
    viewModel: CalendarSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is CalendarSettingsEvent.Saved) {
                onBack()
            }
        }
    }

    Box(
        modifier = Modifier
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
                .padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            CalendarSettingsHeader(onBack = onBack)

            if (uiState.days.isEmpty()) {
                DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "РљР°Р»РµРЅРґР°СЂРЅРёР№ С†РёРєР» Р·Р°РІР°РЅС‚Р°Р¶СѓС”С‚СЊСЃСЏ.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            } else {
                TemplateBlock(
                    selectedTemplate = uiState.selectedTemplate,
                    onTemplateSelected = viewModel::onTemplateSelected
                )
                CycleParametersBlock(
                    uiState = uiState,
                    onCycleNameChanged = viewModel::onCycleNameChanged,
                    onStartDateChanged = viewModel::onStartDateChanged,
                    onCycleLengthIncreased = viewModel::onCycleLengthIncreased,
                    onCycleLengthDecreased = viewModel::onCycleLengthDecreased,
                    onRepeatsChanged = viewModel::onRepeatsChanged
                )
                CycleDaysBlock(
                    days = uiState.days,
                    onDayNameChanged = viewModel::onDayNameChanged,
                    onDayTypeChanged = viewModel::onDayTypeChanged,
                    onAddDay = viewModel::onAddDay,
                    onRemoveDay = viewModel::onRemoveDay,
                    onMoveDayUp = viewModel::onMoveDayUp,
                    onMoveDayDown = viewModel::onMoveDayDown
                )
                CalendarSettingsActions(
                    errorMessage = uiState.errorMessage,
                    isSaving = uiState.isSaving,
                    onSave = viewModel::onSaveCycle,
                    onCancel = onBack
                )
            }
        }
    }
}

@Composable
private fun CalendarSettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SmallGlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBack
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "РќР°Р»Р°С€С‚СѓРІР°РЅРЅСЏ РєР°Р»РµРЅРґР°СЂСЏ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "РћРєСЂРµРјРёР№ РєР°Р»РµРЅРґР°СЂРЅРёР№ С†РёРєР»",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TemplateBlock(
    selectedTemplate: CalendarCycleTemplate,
    onTemplateSelected: (CalendarCycleTemplate) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РЁР°Р±Р»РѕРЅ С†РёРєР»Сѓ",
                subtitle = "РћР±СЂР°РЅРёР№ С€Р°Р±Р»РѕРЅ Р·Р°РїРѕРІРЅСЋС” С„РѕСЂРјСѓ РЅРёР¶С‡Рµ"
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                calendarCycleTemplates.forEach { template ->
                    TemplateOption(
                        template = template,
                        selected = template == selectedTemplate,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateOption(
    template: CalendarCycleTemplate,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) AccentPrimarySoft else Color.White.copy(alpha = 0.026f))
            .border(1.dp, if (selected) BorderActive else BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.Check else Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = if (selected) AccentPrimary else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = template.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CycleParametersBlock(
    uiState: CalendarSettingsUiState,
    onCycleNameChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onCycleLengthIncreased: () -> Unit,
    onCycleLengthDecreased: () -> Unit,
    onRepeatsChanged: (Boolean) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = "РћСЃРЅРѕРІРЅС– РїР°СЂР°РјРµС‚СЂРё",
                subtitle = "Р¦С– РґР°РЅС– СЃС‚РѕСЃСѓСЋС‚СЊСЃСЏ Р»РёС€Рµ РєР°Р»РµРЅРґР°СЂРЅРѕРіРѕ С†РёРєР»Сѓ"
            )
            CalendarTextField(
                label = "РќР°Р·РІР° С†РёРєР»Сѓ",
                value = uiState.cycleName,
                onValueChange = onCycleNameChanged
            )
            CalendarTextField(
                label = "Р”Р°С‚Р° СЃС‚Р°СЂС‚Сѓ С†РёРєР»Сѓ",
                value = uiState.startDateInput,
                onValueChange = onStartDateChanged,
                supportingText = "Р¤РѕСЂРјР°С‚: YYYY-MM-DD"
            )
            CycleLengthRow(
                cycleLength = uiState.cycleLength,
                onDecrease = onCycleLengthDecreased,
                onIncrease = onCycleLengthIncreased
            )
            RepeatRow(
                repeats = uiState.repeats,
                onRepeatsChanged = onRepeatsChanged
            )
        }
    }
}

@Composable
private fun CycleLengthRow(
    cycleLength: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Р”РѕРІР¶РёРЅР° С†РёРєР»Сѓ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$cycleLength РґРЅС–РІ",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
        }
        SmallGlassIconButton(
            icon = Icons.Filled.Remove,
            enabled = cycleLength > 1,
            onClick = onDecrease
        )
        SmallGlassIconButton(
            icon = Icons.Filled.Add,
            onClick = onIncrease,
            active = true
        )
    }
}

@Composable
private fun RepeatRow(
    repeats: Boolean,
    onRepeatsChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "РђРІС‚РѕРјР°С‚РёС‡РЅРµ РїРѕРІС‚РѕСЂРµРЅРЅСЏ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (repeats) "Р¦РёРєР» РїРѕРІС‚РѕСЂСЋС”С‚СЊСЃСЏ РїС–СЃР»СЏ РѕСЃС‚Р°РЅРЅСЊРѕРіРѕ РґРЅСЏ" else "Р¦РёРєР» РЅРµ РїРѕРІС‚РѕСЂСЋС”С‚СЊСЃСЏ",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = repeats,
            onCheckedChange = onRepeatsChanged
        )
    }
}

@Composable
private fun CycleDaysBlock(
    days: List<CalendarCycleDayDraftUiModel>,
    onDayNameChanged: (Int, String) -> Unit,
    onDayTypeChanged: (Int, CalendarCycleDayType) -> Unit,
    onAddDay: () -> Unit,
    onRemoveDay: (Int) -> Unit,
    onMoveDayUp: (Int) -> Unit,
    onMoveDayDown: (Int) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Р”РЅС– С†РёРєР»Сѓ",
                subtitle = "${days.size} РґРЅС–РІ Сѓ РїРѕС‚РѕС‡РЅС–Р№ С„РѕСЂРјС–"
            )
            days.forEach { day ->
                CycleDayEditor(
                    day = day,
                    canRemove = days.size > 1,
                    canMoveUp = day.index > 1,
                    canMoveDown = day.index < days.size,
                    onDayNameChanged = onDayNameChanged,
                    onDayTypeChanged = onDayTypeChanged,
                    onRemoveDay = onRemoveDay,
                    onMoveDayUp = onMoveDayUp,
                    onMoveDayDown = onMoveDayDown
                )
            }
            SystemButton(
                text = "Р”РѕРґР°С‚Рё РґРµРЅСЊ",
                icon = Icons.Filled.Add,
                onClick = onAddDay,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CycleDayEditor(
    day: CalendarCycleDayDraftUiModel,
    canRemove: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onDayNameChanged: (Int, String) -> Unit,
    onDayTypeChanged: (Int, CalendarCycleDayType) -> Unit,
    onRemoveDay: (Int) -> Unit,
    onMoveDayUp: (Int) -> Unit,
    onMoveDayDown: (Int) -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SystemSurfaceGlass.copy(alpha = 0.62f))
            .border(1.dp, BorderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Р”РµРЅСЊ ${day.index}",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
            SmallGlassIconButton(
                icon = Icons.Filled.KeyboardArrowUp,
                enabled = canMoveUp,
                onClick = { onMoveDayUp(day.index) }
            )
            SmallGlassIconButton(
                icon = Icons.Filled.KeyboardArrowDown,
                enabled = canMoveDown,
                onClick = { onMoveDayDown(day.index) }
            )
            SmallGlassIconButton(
                icon = Icons.Filled.Delete,
                enabled = canRemove,
                onClick = { onRemoveDay(day.index) }
            )
        }
        CalendarTextField(
            label = "РќР°Р·РІР° РґРЅСЏ",
            value = day.name,
            onValueChange = { onDayNameChanged(day.index, it) }
        )
        DayTypeSelector(
            selectedType = day.type,
            onTypeSelected = { onDayTypeChanged(day.index, it) }
        )
    }
}

@Composable
private fun DayTypeSelector(
    selectedType: CalendarCycleDayType,
    onTypeSelected: (CalendarCycleDayType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CalendarCycleDayType.values().forEach { type ->
            DayTypeChip(
                type = type,
                selected = type == selectedType,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun DayTypeChip(
    type: CalendarCycleDayType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Text(
        text = type.toDisplayText(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = if (selected) AccentPrimary else TextSecondary,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) AccentPrimarySoft else Color.White.copy(alpha = 0.026f))
            .border(1.dp, if (selected) BorderActive else BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun CalendarSettingsActions(
    errorMessage: UiText?,
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage.asString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AccentWarning,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SystemButton(
                    text = "РЎРєР°СЃСѓРІР°С‚Рё",
                    icon = Icons.Filled.Close,
                    onClick = onCancel,
                    accent = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = if (isSaving) "Р—Р±РµСЂРµР¶РµРЅРЅСЏ" else "Р—Р±РµСЂРµРіС‚Рё С†РёРєР»",
                    icon = Icons.Filled.Save,
                    onClick = onSave,
                    enabled = !isSaving,
                    glow = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CalendarTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { text ->
            { Text(text = text, color = TextMuted) }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentPrimary,
            focusedBorderColor = AccentPrimary.copy(alpha = 0.62f),
            unfocusedBorderColor = BorderSubtle,
            focusedLabelColor = AccentPrimary,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = Color.White.copy(alpha = 0.028f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.018f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SmallGlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(
                when {
                    !enabled -> Color.White.copy(alpha = 0.018f)
                    active -> AccentPrimarySoft
                    else -> Color.White.copy(alpha = 0.035f)
                }
            )
            .border(1.dp, if (active && enabled) BorderActive else BorderSubtle, shape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> TextMuted.copy(alpha = 0.44f)
                active -> AccentPrimary
                else -> TextSecondary
            },
            modifier = Modifier.size(19.dp)
        )
    }
}

private val calendarCycleTemplates = listOf(
    CalendarCycleTemplate.FIVE_TWO,
    CalendarCycleTemplate.DAY_NIGHT_RECOVERY_OFF,
    CalendarCycleTemplate.TWO_TWO,
    CalendarCycleTemplate.CUSTOM
)

private fun CalendarCycleDayType.toDisplayText(): String =
    when (this) {
        CalendarCycleDayType.WORK -> "Р РѕР±РѕС‡РёР№"
        CalendarCycleDayType.NIGHT -> "РќС–С‡РЅРёР№"
        CalendarCycleDayType.RECOVERY -> "Р’С–РґСЃРёРїРЅРёР№"
        CalendarCycleDayType.OFF -> "Р’РёС…С–РґРЅРёР№"
        CalendarCycleDayType.CUSTOM -> "Р’Р»Р°СЃРЅРёР№"
    }
