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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarCycleTemplate
import com.ihor.thesystem.domain.model.title
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarCycleDayDraftUiModel
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarSettingsEvent
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarSettingsUiState
import com.ihor.thesystem.feature.calendar.viewmodel.CalendarSettingsViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop

@Composable
fun CalendarSettingsScreen(
    onBack: () -> Unit,
    viewModel: CalendarSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors

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
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            CalendarSettingsHeader(onBack = onBack)

            if (uiState.days.isEmpty()) {
                DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Календарний цикл завантажується.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
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
                TodayCycleAnchorBlock(
                    days = uiState.days,
                    todayCycleDayIndex = uiState.todayCycleDayIndex,
                    onTodayCycleDaySelected = viewModel::onTodayCycleDaySelected
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
    val colors = SystemTheme.colors
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
                text = "Налаштування календаря",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Окремий календарний цикл",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
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
                title = "Шаблон циклу",
                subtitle = "Обраний шаблон заповнює форму нижче"
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.accentPrimarySoft else colors.surfaceGlassSoft)
            .border(1.dp, if (selected) colors.borderActive else colors.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.Check else Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = if (selected) colors.accentPrimary else colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = template.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
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
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            SystemSectionHeader(
                title = "Основні параметри",
                subtitle = "Ці дані стосуються лише календарного циклу"
            )
            CalendarTextField(
                label = "Назва циклу",
                value = uiState.cycleName,
                onValueChange = onCycleNameChanged
            )
            CalendarTextField(
                label = "Дата старту циклу",
                value = uiState.startDateInput,
                onValueChange = onStartDateChanged,
                supportingText = "Формат: YYYY-MM-DD"
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
private fun TodayCycleAnchorBlock(
    days: List<CalendarCycleDayDraftUiModel>,
    todayCycleDayIndex: Int?,
    onTodayCycleDaySelected: (Int) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = todayCycleDayIndex != null) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Який день сьогодні?",
                subtitle = todayCycleDayIndex?.let { "Сьогодні активний День $it" }
                    ?: "Оберіть день поточного циклу"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    TodayCycleDayChip(
                        day = day,
                        selected = day.index == todayCycleDayIndex,
                        onClick = { onTodayCycleDaySelected(day.index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayCycleDayChip(
    day: CalendarCycleDayDraftUiModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .width(156.dp)
            .clip(shape)
            .background(if (selected) colors.accentPrimarySoft else colors.surfaceGlassSoft)
            .border(1.dp, if (selected) colors.borderActive else colors.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.Check else Icons.Filled.Today,
            contentDescription = null,
            tint = if (selected) colors.accentPrimary else colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "День ${day.index}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (selected) colors.accentPrimary else colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = day.name.ifBlank { day.type.toDisplayText() },
                style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Довжина циклу",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$cycleLength днів",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
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
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Автоматичне повторення",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (repeats) {
                    "Цикл повторюється після останнього дня"
                } else {
                    "Цикл не повторюється"
                },
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
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
                title = "Дні циклу",
                subtitle = "${days.size} днів у поточній формі"
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
                text = "Додати день",
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemItemSpacing),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "День ${day.index}",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = colors.textPrimary,
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
            label = "Назва дня",
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Text(
        text = type.toDisplayText(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = if (selected) colors.accentPrimary else colors.textSecondary,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) colors.accentPrimarySoft else colors.surfaceGlassSoft)
            .border(1.dp, if (selected) colors.borderActive else colors.borderSubtle, shape)
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
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage.asString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.accentWarning,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SystemButton(
                    text = "Скасувати",
                    icon = Icons.Filled.Close,
                    onClick = onCancel,
                    accent = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = if (isSaving) "Збереження" else "Зберегти цикл",
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
    val colors = SystemTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { text ->
            { Text(text = text, color = colors.textMuted) }
        },
        singleLine = true,
        shape = RoundedCornerShape(SystemTheme.shapes.medium),
        colors = systemOutlinedTextFieldColors(),
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(
                when {
                    !enabled -> colors.overlayLight.copy(alpha = 0.26f)
                    active -> colors.accentPrimarySoft
                    else -> colors.overlayLight
                }
            )
            .border(1.dp, if (active && enabled) colors.borderActive else colors.borderSubtle, shape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> colors.textMuted.copy(alpha = 0.44f)
                active -> colors.accentPrimary
                else -> colors.textSecondary
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
        CalendarCycleDayType.WORK -> "Робочий"
        CalendarCycleDayType.NIGHT -> "Нічний"
        CalendarCycleDayType.RECOVERY -> "Відсипний"
        CalendarCycleDayType.OFF -> "Вихідний"
        CalendarCycleDayType.CUSTOM -> "Власний"
    }
