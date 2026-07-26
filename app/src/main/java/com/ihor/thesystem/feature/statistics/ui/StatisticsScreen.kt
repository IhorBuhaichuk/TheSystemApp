package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.components.SystemStateKind
import com.ihor.thesystem.core.ui.components.SystemStatePanel
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors

    RefreshOnResume(viewModel::refreshForCurrentDay)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        when (val state = uiState) {
            UiState.Loading -> {
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
            }

            is UiState.Content -> {
                AnalyticsDashboard(
                    data = state.data,
                    onOpenAnnualProgression = { navController.navigate(Routes.AnnualProgressionDetails) },
                    onLogWorkout = { navController.navigate(Routes.Cycle) }
                )
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SystemScreenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    SystemStatePanel(
                        kind = SystemStateKind.Error,
                        title = "Аналітика недоступна",
                        message = state.message.asString(context),
                        actionLabel = "Повторити",
                        onAction = viewModel::refreshForCurrentDay,
                        modifier = Modifier.fillMaxSize(0.72f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsDashboard(
    data: StatisticsUiData,
    onOpenAnnualProgression: () -> Unit,
    onLogWorkout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SystemUiTestTags.STATISTICS_SCROLL),
        contentPadding = PaddingValues(
            start = SystemScreenPadding,
            top = SystemCardPadding,
            end = SystemScreenPadding,
            bottom = SystemScreenPadding + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        item(key = "header") { AnalyticsHeader() }
        item(key = "weekly_report") {
            WeeklySystemReportBlock(report = data.weeklySystemReport)
        }
        item(key = "summary") { AnalyticsSummaryBlock(data) }
        item(key = "usage") { BetaMetricsBlock(metrics = data.betaMetrics) }
        item(key = "weekly_summary") {
            WeeklySummaryBlock(
                days = data.weeklySummary.days,
                totalTonnage = data.weeklySummary.totalTonnage,
                onLogWorkout = onLogWorkout
            )
        }
        item(key = "annual_progression") {
            AnnualProgressionBlock(
                data = data,
                onOpenAnnualProgression = onOpenAnnualProgression
            )
        }
        item(key = "system_insight") {
            DeterministicSystemInsightBlock(insight = data.systemInsight)
        }
    }
}

@Composable
private fun AnalyticsHeader() {
    val colors = SystemTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(SystemUiTestTags.STATISTICS_HEADER)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                lineHeight = 30.sp
            )
        )
        Text(
            text = "Підсумок і прогрес",
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
        )
    }
}
