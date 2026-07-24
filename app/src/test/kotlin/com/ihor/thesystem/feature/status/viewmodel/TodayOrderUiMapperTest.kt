package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayOrderUiMapperTest {

    @Test
    fun `maps standard training into clear training order`() {
        val order = decision(
            type = TodayTrainingDecisionType.STANDARD_TRAINING,
            readinessScore = 72,
            workoutName = "Workout A"
        ).toTodayOrderUiModel(mainQuest())

        assertEquals(TodayOrderDayType.TRAINING, order.dayType)
        assertEquals("Training", order.dayTypeLabel)
        assertEquals("Workout A", order.title)
        assertEquals("Readiness 72% підходить під поточний план. Система не бачить блокерів.", order.reason)
        assertEquals("Відкрити тренування", order.primaryActionLabel)
        assertEquals("+75 XP", order.outcomeText)
        assertEquals("45 хв", order.durationText)
        assertEquals(TodayOrderAccent.PRIMARY, order.accent)
        assertEquals(0.72f, order.readinessProgress, 0.001f)
        assertTrue(order.actionEnabled)
    }

    @Test
    fun `maps recovery decision into recovery order`() {
        val order = decision(
            type = TodayTrainingDecisionType.ACTIVE_RECOVERY,
            readinessScore = 48,
            debtLevel = RecoveryDebtLevel.CRITICAL
        ).toTodayOrderUiModel(null)

        assertEquals(TodayOrderDayType.RECOVERY, order.dayType)
        assertEquals("Recovery", order.dayTypeLabel)
        assertEquals("Recovery Protocol", order.title)
        assertTrue(order.reason.contains("силове навантаження сьогодні заблоковано"))
        assertEquals("Відкрити recovery", order.primaryActionLabel)
        assertEquals("Recovery + streak", order.outcomeText)
        assertEquals("12 хв", order.durationText)
        assertEquals(TodayOrderAccent.SUCCESS, order.accent)
        assertTrue(order.actionEnabled)
    }

    @Test
    fun `maps no excuse missed reason into no excuse order`() {
        val order = decision(
            type = TodayTrainingDecisionType.NO_EXCUSE,
            readinessScore = 58,
            reason = "missed planned workout"
        ).toTodayOrderUiModel(null)

        assertEquals(TodayOrderDayType.NO_EXCUSE, order.dayType)
        assertEquals("No Excuse", order.dayTypeLabel)
        assertEquals("No Excuse Protocol", order.title)
        assertTrue(order.reason.contains("зафіксувала пропуск"))
        assertEquals("Відкрити 7 хв", order.primaryActionLabel)
        assertEquals("Streak + quest", order.outcomeText)
        assertEquals(TodayOrderAccent.ERROR, order.accent)
    }

    @Test
    fun `maps deload and rest into separate day types`() {
        val deload = decision(TodayTrainingDecisionType.DELOAD)
            .toTodayOrderUiModel(null)
        val rest = decision(TodayTrainingDecisionType.REST)
            .toTodayOrderUiModel(null)

        assertEquals(TodayOrderDayType.DELOAD, deload.dayType)
        assertEquals("Відкрити deload", deload.primaryActionLabel)
        assertEquals("Recovery + контроль XP", deload.outcomeText)
        assertEquals(TodayOrderAccent.WARNING, deload.accent)

        assertEquals(TodayOrderDayType.REST, rest.dayType)
        assertEquals("Rest", rest.dayTypeLabel)
        assertEquals("Відкрити план дня", rest.primaryActionLabel)
        assertEquals("Recovery без штрафу", rest.outcomeText)
        assertEquals(TodayOrderAccent.AI, rest.accent)
    }

    @Test
    fun `maps missing decision into soft fallback without empty state`() {
        val order = (null as TodayTrainingDecision?)
            .toTodayOrderUiModel(mainQuest(title = "Workout B", minutes = 38, xp = 60))

        assertEquals(TodayOrderDayType.TRAINING, order.dayType)
        assertEquals("Training", order.dayTypeLabel)
        assertEquals("Workout B", order.title)
        assertTrue(order.reason.contains("Readiness ще синхронізується"))
        assertEquals("План формується", order.primaryActionLabel)
        assertEquals("+60 XP", order.outcomeText)
        assertEquals("38 хв", order.durationText)
        assertEquals(0f, order.readinessProgress, 0.001f)
        assertFalse(order.actionEnabled)
    }

    private fun mainQuest(
        title: String = "Workout A",
        minutes: Int = 45,
        xp: Int = 75
    ): QuestUiModel =
        QuestUiModel(
            id = 1,
            title = title,
            subtitle = UiText.DynamicString("main"),
            tasks = persistentListOf(),
            estimatedDurationMinutes = minutes,
            rewardXp = xp
        )

    private fun decision(
        type: TodayTrainingDecisionType,
        readinessScore: Int = 72,
        workoutName: String? = "Workout A",
        reason: String = "domain reason",
        debtLevel: RecoveryDebtLevel = RecoveryDebtLevel.LOW
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = 0L,
            cycleDay = 1,
            workoutName = workoutName,
            readinessScore = readinessScore,
            readinessLevel = ReadinessLevel.STANDARD,
            recoveryDebt = RecoveryDebt(
                value = 12,
                level = debtLevel,
                reasons = listOf("test")
            ),
            decisionType = type,
            loadMultiplier = 1f,
            volumeMultiplier = 1f,
            reason = reason,
            warnings = emptyList(),
            selectedWorkoutTemplateId = 1,
            isTrainingAllowed = type !in setOf(
                TodayTrainingDecisionType.ACTIVE_RECOVERY,
                TodayTrainingDecisionType.REST
            )
        )
}
