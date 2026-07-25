package com.ihor.thesystem.feature.profile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import java.util.Locale
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.SystemAvatarBadge
import com.ihor.thesystem.core.ui.components.SystemMetricBlock
import com.ihor.thesystem.core.ui.components.SystemPanel
import com.ihor.thesystem.core.ui.components.SystemStateKind
import com.ihor.thesystem.core.ui.components.SystemStatePanel
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionTitle
import com.ihor.thesystem.core.ui.components.SystemSettingsRow
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.EditNameDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogAgeDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogHeightDialog
import com.ihor.thesystem.feature.statistics.ui.components.dialogs.LogWeightDialog
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsDialogState
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsViewModel
import com.ihor.thesystem.feature.status.ui.WorkoutDialogHost
import com.ihor.thesystem.feature.status.viewmodel.StatusDialogState
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusViewModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    navController: NavHostController,
    statusViewModel: StatusViewModel = hiltViewModel(),
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel()
) {
    val statusState by statusViewModel.uiState.collectAsStateWithLifecycle()
    val statusDialogState by statusViewModel.dialogState.collectAsStateWithLifecycle()
    val statisticsState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
    val statisticsDialogState by statisticsViewModel.dialogState.collectAsStateWithLifecycle()
    val activeWorkout by workoutViewModel.activeWorkoutState.collectAsStateWithLifecycle()
    val workoutDialogState by workoutViewModel.dialogState.collectAsStateWithLifecycle()
    val settingsUiState by workoutViewModel.settingsUiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors
    val statusData = (statusState as? UiState.Content<StatusUiData>)?.data
    val statisticsData = (statisticsState as? UiState.Content<StatisticsUiData>)?.data

    RefreshOnResume(statusViewModel::refreshForCurrentDay)
    RefreshOnResume(statisticsViewModel::refreshForCurrentDay)
    RefreshOnResume(workoutViewModel::refreshForCurrentDay)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        if (statusData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SystemScreenPadding),
                contentAlignment = Alignment.Center
            ) {
                SystemStatePanel(
                    kind = SystemStateKind.Loading,
                    modifier = Modifier.fillMaxSize(0.42f)
                )
            }
        } else {
            ProfileDashboard(
                statusData = statusData,
                statisticsData = statisticsData,
                onAvatarSelected = statusViewModel::updateAvatarUri,
                onEditName = statusViewModel::onEditNameTap,
                onOpenWeight = statisticsViewModel::onOpenLogWeight,
                onOpenHeight = statisticsViewModel::onOpenEditHeight,
                onOpenAge = statisticsViewModel::onOpenEditAge,
                onOpenWorkoutSettings = workoutViewModel::onOpenWorkoutSettings,
                onOpenCalendarSettings = { navController.navigate(Routes.CalendarSettings) },
                onOpenStatistics = { navController.navigate(Routes.Statistics) }
            )
        }

        if (statusDialogState is StatusDialogState.EditName && statusData != null) {
            EditNameDialog(
                currentName = statusData.playerName,
                onConfirm = statusViewModel::onNameConfirmed,
                onDismiss = statusViewModel::onDismissDialog
            )
        }

        ProfileStatisticsDialogs(
            dialogState = statisticsDialogState,
            data = statisticsData,
            onWeightConfirmed = statisticsViewModel::onWeightConfirmed,
            onHeightConfirmed = statisticsViewModel::onHeightConfirmed,
            onAgeConfirmed = statisticsViewModel::onAgeConfirmed,
            onDismiss = statisticsViewModel::onDismissDialog
        )

        WorkoutDialogHost(
            dialogState = workoutDialogState,
            activeDayWorkout = activeWorkout,
            settingsUiState = settingsUiState,
            workoutViewModel = workoutViewModel,
            onOpenWorkoutAnalysis = { sessionId ->
                navController.navigate(Routes.WorkoutAnalysis(sessionId = sessionId))
            }
        )
    }
}

@Composable
private fun ProfileDashboard(
    statusData: StatusUiData,
    statisticsData: StatisticsUiData?,
    onAvatarSelected: (android.net.Uri) -> Unit,
    onEditName: () -> Unit,
    onOpenWeight: () -> Unit,
    onOpenHeight: () -> Unit,
    onOpenAge: () -> Unit,
    onOpenWorkoutSettings: () -> Unit,
    onOpenCalendarSettings: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SystemUiTestTags.PROFILE_SCROLL),
        contentPadding = PaddingValues(
            start = SystemScreenPadding,
            top = SystemCardPadding,
            end = SystemScreenPadding,
            bottom = SystemScreenPadding + 4.dp
        ),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        item(key = "hero") {
            ProfileHeroPanel(
                statusData = statusData,
                onAvatarSelected = onAvatarSelected,
                onEditName = onEditName
            )
        }
        item(key = "personal_metrics") {
            PersonalMetricsPanel(
                statusData = statusData,
                statisticsData = statisticsData,
                onOpenWeight = onOpenWeight,
                onOpenHeight = onOpenHeight,
                onOpenAge = onOpenAge
            )
        }
        item(key = "achievements") {
            AchievementsPanel(statusData = statusData)
        }
        item(key = "settings") {
            SettingsPanel(
                onOpenWorkoutSettings = onOpenWorkoutSettings,
                onOpenCalendarSettings = onOpenCalendarSettings,
                onOpenStatistics = onOpenStatistics
            )
        }
    }
}

@Composable
private fun ProfileHeroPanel(
    statusData: StatusUiData,
    onAvatarSelected: (android.net.Uri) -> Unit,
    onEditName: () -> Unit
) {
    val colors = SystemTheme.colors
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(onAvatarSelected) }
    )
    val progress = if (statusData.xpMax > 0) {
        statusData.xpTotal.toFloat() / statusData.xpMax.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)

    SystemPanel(
        active = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(SystemUiTestTags.PROFILE_HERO)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 400.dp
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SystemCardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SystemAvatarBadge(
                        avatarUri = statusData.avatarUri,
                        modifier = Modifier.size(if (compact) 104.dp else 132.dp),
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SystemSectionTitle(title = "Профіль")
                        Text(
                            text = statusData.playerName.toSystemSentenceCase(),
                            modifier = Modifier.systemClickable(onClick = onEditName),
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Мисливець рангу ${statusData.globalRank.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!compact) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RankMiniBox(
                                    label = "Ранг",
                                    value = statusData.globalRank.name,
                                    modifier = Modifier.width(86.dp)
                                )
                                RankMiniBox(
                                    label = "Рівень",
                                    value = statusData.level.toString(),
                                    modifier = Modifier.width(86.dp)
                                )
                            }
                        }
                    }
                }

                if (compact) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SystemCardPadding),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RankMiniBox(
                            label = "Ранг",
                            value = statusData.globalRank.name,
                            modifier = Modifier.weight(1f)
                        )
                        RankMiniBox(
                            label = "Рівень",
                            value = statusData.level.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(top = SystemCardPadding),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${statusData.xpTotal} / ${statusData.xpMax} XP",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.accentPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = "${(progress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    SystemProgressBar(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankMiniBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    SystemMetricBlock(
        label = label,
        value = value,
        modifier = modifier.heightIn(min = 74.dp)
    )
}

@Composable
private fun PersonalMetricsPanel(
    statusData: StatusUiData,
    statisticsData: StatisticsUiData?,
    onOpenWeight: () -> Unit,
    onOpenHeight: () -> Unit,
    onOpenAge: () -> Unit
) {
    val colors = SystemTheme.colors
    val weight = statisticsData?.currentWeight?.takeIf { it > 0f } ?: statusData.currentWeight
    val height = statisticsData?.currentHeight?.takeIf { it > 0f } ?: statusData.height
    val age = statisticsData?.age?.takeIf { it > 0 }

    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            SystemSectionTitle(title = "Особисті показники")
            SystemSettingsRow(
                title = "Поточна вага",
                subtitle = weight?.let { "${it.formatCompact()} кг" } ?: "Не задано",
                icon = Icons.Filled.FitnessCenter,
                accent = colors.accentPrimary,
                onClick = onOpenWeight
            )
            SystemSettingsRow(
                title = "Зріст",
                subtitle = height?.let { "${it.formatCompact()} см" } ?: "Не задано",
                icon = Icons.Filled.Height,
                accent = colors.accentAi,
                onClick = onOpenHeight
            )
            SystemSettingsRow(
                title = "Вік",
                subtitle = age?.let { "$it" } ?: "Не задано",
                icon = Icons.Filled.Cake,
                accent = colors.accentSuccess,
                onClick = onOpenAge
            )
        }
    }
}

@Composable
private fun AchievementsPanel(statusData: StatusUiData) {
    val colors = SystemTheme.colors
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            SystemSectionTitle(title = "Досягнення")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SystemMetricBlock(
                    label = "Серія",
                    value = statusData.currentStreak.toString(),
                    subtitle = "днів",
                    accent = colors.accentSuccess,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricBlock(
                    label = "Макс.",
                    value = statusData.maxStreak.toString(),
                    subtitle = "днів",
                    accent = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                SystemMetricBlock(
                    label = "Квести",
                    value = statusData.todos.count { it.isCompleted }.toString(),
                    subtitle = "закрито",
                    accent = colors.accentAi,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    onOpenWorkoutSettings: () -> Unit,
    onOpenCalendarSettings: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    val colors = SystemTheme.colors
    SystemPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SystemSectionTitle(title = "Налаштування")
            SystemSettingsRow(
                title = "Налаштування тренувань",
                subtitle = "Цикл, вправи, обладнання, дані сну та резервна копія",
                icon = Icons.Filled.FitnessCenter,
                accent = colors.accentPrimary,
                onClick = onOpenWorkoutSettings
            )
            SystemSettingsRow(
                title = "Налаштування календаря",
                subtitle = "Робочі дні, фази та повторення календарного циклу",
                icon = Icons.Filled.CalendarToday,
                accent = colors.accentAi,
                onClick = onOpenCalendarSettings
            )
            SystemSettingsRow(
                title = "Статистика профілю",
                subtitle = "Вага, прогресія та аналітичні дані",
                icon = Icons.Filled.AccountCircle,
                accent = colors.accentSuccess,
                onClick = onOpenStatistics
            )
        }
    }
}

@Composable
private fun ProfileStatisticsDialogs(
    dialogState: StatisticsDialogState,
    data: StatisticsUiData?,
    onWeightConfirmed: (Float) -> Unit,
    onHeightConfirmed: (Float) -> Unit,
    onAgeConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    when (dialogState) {
        is StatisticsDialogState.LogWeight -> LogWeightDialog(
            currentWeight = data?.currentWeight ?: 0f,
            onConfirm = onWeightConfirmed,
            onDismiss = onDismiss
        )
        is StatisticsDialogState.EditHeight -> LogHeightDialog(
            currentHeight = data?.currentHeight ?: 0f,
            onConfirm = onHeightConfirmed,
            onDismiss = onDismiss
        )
        is StatisticsDialogState.EditAge -> LogAgeDialog(
            currentAge = data?.age ?: 0,
            onConfirm = onAgeConfirmed,
            onDismiss = onDismiss
        )
        else -> Unit
    }
}

private fun Float.formatCompact(): String =
    if (this % 1f == 0f) toInt().toString() else String.format(Locale.US, "%.1f", this)
