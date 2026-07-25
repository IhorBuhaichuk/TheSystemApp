package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.core.ui.components.systemToggleable
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.feature.status.viewmodel.BackupUiState
import com.ihor.thesystem.feature.status.viewmodel.HealthConnectUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun EquipmentSettingsPanel(
    profile: EquipmentProfile,
    dumbbellMaxKgDraft: String,
    healthConnect: HealthConnectUiState,
    backup: BackupUiState,
    onLocationChanged: (Boolean) -> Unit,
    onEquipmentAvailabilityChanged: (EquipmentType, Boolean) -> Unit,
    onDumbbellMaxKgChanged: (String) -> Unit,
    onConnectHealthConnect: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = modifier.fillMaxWidth(), contentPadding = 0.dp) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(SystemCardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SystemSectionHeader(
                    title = "Обладнання",
                    subtitle = if (profile.trainsAtGym) "Зал або повний доступ" else "Домашній профіль"
                )
            }
            item {
                HealthConnectSettingsBlock(
                    state = healthConnect,
                    onConnect = onConnectHealthConnect
                )
            }
            item {
                BackupSettingsBlock(
                    state = backup,
                    onExport = onExportBackup,
                    onImport = onImportBackup
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ScheduleSettingsTab(
                        text = "Дім",
                        selected = !profile.trainsAtGym,
                        onClick = { onLocationChanged(false) },
                        modifier = Modifier.weight(1f)
                    )
                    ScheduleSettingsTab(
                        text = "Зал",
                        selected = profile.trainsAtGym,
                        onClick = { onLocationChanged(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = dumbbellMaxKgDraft,
                    onValueChange = onDumbbellMaxKgChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Макс. гантель, кг",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(SystemTheme.shapes.medium),
                    colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary)
                )
            }
            items(EQUIPMENT_OPTIONS, key = { it.type.name }) { option ->
                EquipmentToggleRow(
                    label = option.label,
                    checked = profile.isEquipmentEnabled(option.type),
                    onCheckedChange = { checked -> onEquipmentAvailabilityChanged(option.type, checked) }
                )
            }
        }
    }
}

@Composable
private fun HealthConnectSettingsBlock(
    state: HealthConnectUiState,
    onConnect: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    val accent = when {
        state.hasReadinessPermission -> colors.accentSuccess
        state.isAvailable -> colors.accentPrimary
        else -> colors.textMuted
    }
    val statusText = when {
        state.isLoading -> "Перевірка статусу"
        state.hasReadinessPermission -> "Підключено: сон може уточнювати readiness"
        state.isAvailable -> "Доступно: потрібен дозвіл на сон"
        else -> "Health Connect недоступний на цьому пристрої"
    }
    val buttonText = when {
        state.hasReadinessPermission -> "Підключено"
        state.isLoading -> "Перевірка"
        else -> "Connect"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SystemSectionHeader(
            title = "Health Connect",
            subtitle = statusText
        )
        Text(
            text = "Дані здоров'я лишаються локальним сигналом readiness.",
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
        )
        SystemButton(
            text = buttonText,
            icon = Icons.Filled.FitnessCenter,
            onClick = onConnect,
            enabled = state.isAvailable && !state.hasReadinessPermission && !state.isLoading,
            accent = accent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BackupSettingsBlock(
    state: BackupUiState,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    val lastBackupText = state.lastExportedAtMillis
        ?.let { "Останній backup: ${it.backupTimeLabel()}" }
        ?: state.lastImportedAtMillis?.let { "Останній імпорт: ${it.backupTimeLabel()}" }
        ?: "Останній backup: ще не створено"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SystemSectionHeader(
            title = "Backup даних",
            subtitle = lastBackupText
        )
        Text(
            text = "Експортуються тренування, профіль, матриця прогресу, readiness, обладнання, календар, задачі та історія квестів.",
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SystemButton(
                text = if (state.isBusy) "Обробка" else "Експорт даних",
                icon = Icons.Filled.Save,
                onClick = onExport,
                enabled = !state.isBusy,
                accent = colors.accentPrimary,
                modifier = Modifier.weight(1f)
            )
            SystemButton(
                text = "Імпорт даних",
                icon = Icons.Filled.Add,
                onClick = onImport,
                enabled = !state.isBusy,
                accent = colors.accentWarning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun Long.backupTimeLabel(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(BACKUP_TIME_FORMATTER)

private val BACKUP_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

@Composable
private fun EquipmentToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .systemToggleable(
                value = checked,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class EquipmentOption(
    val type: EquipmentType,
    val label: String
)

private val EQUIPMENT_OPTIONS = listOf(
    EquipmentOption(EquipmentType.DUMBBELL, "Гантелі"),
    EquipmentOption(EquipmentType.BARBELL, "Штанга"),
    EquipmentOption(EquipmentType.BENCH, "Лава"),
    EquipmentOption(EquipmentType.PULL_UP_BAR, "Турнік"),
    EquipmentOption(EquipmentType.DIP_BARS, "Бруси"),
    EquipmentOption(EquipmentType.BANDS, "Резинки"),
    EquipmentOption(EquipmentType.MACHINE, "Тренажери / кабелі"),
    EquipmentOption(EquipmentType.KETTLEBELL, "Гиря"),
    EquipmentOption(EquipmentType.EXERCISE_BALL, "Фітбол"),
    EquipmentOption(EquipmentType.FOAM_ROLL, "Ролер")
)

private fun EquipmentProfile.isEquipmentEnabled(type: EquipmentType): Boolean =
    when (type) {
        EquipmentType.BODY_ONLY -> true
        EquipmentType.BARBELL -> barbellAvailable || type in availableEquipment
        EquipmentType.BENCH -> benchAvailable || type in availableEquipment
        EquipmentType.PULL_UP_BAR -> pullUpBarAvailable || type in availableEquipment
        EquipmentType.DIP_BARS -> dipBarsAvailable || type in availableEquipment
        EquipmentType.BANDS -> bandsAvailable || type in availableEquipment
        EquipmentType.MACHINE,
        EquipmentType.CABLE -> machinesAvailable || type in availableEquipment
        else -> type in availableEquipment
    }
