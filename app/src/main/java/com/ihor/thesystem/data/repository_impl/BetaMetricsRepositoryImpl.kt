package com.ihor.thesystem.data.repository_impl

import android.content.Context
import com.ihor.thesystem.domain.model.BetaMetricsEventState
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.repository.BetaMetricsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class BetaMetricsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : BetaMetricsRepository {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val state = MutableStateFlow(loadState())

    override fun observeEventState(): Flow<BetaMetricsEventState> =
        state.asStateFlow()

    override suspend fun markAppOpened(epochDay: Long) {
        updateState { current ->
            current.copy(appOpenedEpochDays = current.appOpenedEpochDays + epochDay)
        }
    }

    override suspend fun recordTodayOrderDecision(
        epochDay: Long,
        decisionType: TodayTrainingDecisionType
    ) {
        updateState { current ->
            current.copy(
                todayOrderDecisionsByDay = current.todayOrderDecisionsByDay
                    .filterKeys { it != epochDay } + (epochDay to decisionType)
            )
        }
    }

    @Synchronized
    private fun updateState(transform: (BetaMetricsEventState) -> BetaMetricsEventState) {
        val next = transform(loadState())
        saveState(next)
        state.value = next
    }

    private fun loadState(): BetaMetricsEventState =
        BetaMetricsEventState(
            appOpenedEpochDays = preferences.getStringSet(KEY_APP_OPENED_DAYS, emptySet()).orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet(),
            todayOrderDecisionsByDay = preferences.getStringSet(KEY_DECISION_SNAPSHOTS, emptySet()).orEmpty()
                .mapNotNull { value ->
                    val epochDay = value.substringBefore(SEPARATOR).toLongOrNull()
                    val decisionType = value.substringAfter(SEPARATOR, "")
                        .takeIf { it.isNotBlank() }
                        ?.let { runCatching { TodayTrainingDecisionType.valueOf(it) }.getOrNull() }
                    if (epochDay != null && decisionType != null) epochDay to decisionType else null
                }
                .toMap()
        )

    private fun saveState(state: BetaMetricsEventState) {
        preferences.edit()
            .putStringSet(
                KEY_APP_OPENED_DAYS,
                state.appOpenedEpochDays.map { it.toString() }.toSet()
            )
            .putStringSet(
                KEY_DECISION_SNAPSHOTS,
                state.todayOrderDecisionsByDay.map { (epochDay, decisionType) ->
                    "$epochDay$SEPARATOR${decisionType.name}"
                }.toSet()
            )
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "the_system_beta_metrics"
        const val KEY_APP_OPENED_DAYS = "app_opened_epoch_days"
        const val KEY_DECISION_SNAPSHOTS = "today_order_decision_snapshots"
        const val SEPARATOR = ":"
    }
}
