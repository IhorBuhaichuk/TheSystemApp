package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType

internal fun TodayTrainingDecision?.toTodayOrderUiModel(
    fallbackMainQuest: QuestUiModel?
): TodayOrderUiModel {
    val decision = this ?: return TodayOrderUiModel(
        dayType = TodayOrderDayType.TRAINING,
        dayTypeLabel = "Training",
        title = fallbackMainQuest?.title ?: "План синхронізується",
        reason = "Readiness ще синхронізується. Система показує базовий план і не піднімає навантаження без даних.",
        primaryActionLabel = "План формується",
        outcomeText = fallbackMainQuest.rewardText("Безпечний fallback"),
        durationText = fallbackMainQuest.durationText("План"),
        readinessProgress = 0f,
        actionEnabled = false,
        accent = TodayOrderAccent.PRIMARY
    )

    return when (decision.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> decision.toTrainingOrder(
            fallbackMainQuest = fallbackMainQuest,
            reasonText = "Readiness ${decision.readinessScore}% і recovery debt ${decision.recoveryDebt.level.displayLabel()}: можна атакувати прогрес.",
            actionText = "Відкрити тренування",
            outcomeText = fallbackMainQuest.rewardText("XP + quest"),
            accent = TodayOrderAccent.SUCCESS
        )
        TodayTrainingDecisionType.STANDARD_TRAINING -> decision.toTrainingOrder(
            fallbackMainQuest = fallbackMainQuest,
            reasonText = "Readiness ${decision.readinessScore}% підходить під поточний план. Система не бачить блокерів.",
            actionText = "Відкрити тренування",
            outcomeText = fallbackMainQuest.rewardText("XP + quest")
        )
        TodayTrainingDecisionType.REDUCED_LOAD -> decision.toTrainingOrder(
            fallbackMainQuest = fallbackMainQuest,
            reasonText = "Readiness ${decision.readinessScore}% або recovery debt ${decision.recoveryDebt.level.displayLabel()}: працюємо легше, без зриву циклу.",
            actionText = "Відкрити легкий план",
            outcomeText = "XP без debt",
            accent = TodayOrderAccent.WARNING
        )
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> TodayOrderUiModel(
            dayType = TodayOrderDayType.RECOVERY,
            dayTypeLabel = "Recovery",
            title = "Recovery Protocol",
            reason = "Readiness ${decision.readinessScore}% або recovery debt ${decision.recoveryDebt.level.displayLabel()}: силове навантаження сьогодні заблоковано.",
            primaryActionLabel = "Відкрити recovery",
            outcomeText = "Recovery + streak",
            durationText = fallbackMainQuest.durationText("12 хв"),
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.SUCCESS
        )
        TodayTrainingDecisionType.NO_EXCUSE -> TodayOrderUiModel(
            dayType = TodayOrderDayType.NO_EXCUSE,
            dayTypeLabel = "No Excuse",
            title = "No Excuse Protocol",
            reason = decision.noExcuseReason(),
            primaryActionLabel = "Відкрити 7 хв",
            outcomeText = "Streak + quest",
            durationText = fallbackMainQuest.durationText("7 хв"),
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.ERROR
        )
        TodayTrainingDecisionType.DELOAD -> TodayOrderUiModel(
            dayType = TodayOrderDayType.DELOAD,
            dayTypeLabel = "Deload",
            title = "Deload Session",
            reason = "Recovery debt ${decision.recoveryDebt.level.displayLabel()}: зменшуємо інтенсивність, щоб зберегти цикл.",
            primaryActionLabel = "Відкрити deload",
            outcomeText = "Recovery + контроль XP",
            durationText = fallbackMainQuest.durationText("30 хв"),
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.WARNING
        )
        TodayTrainingDecisionType.REST -> TodayOrderUiModel(
            dayType = TodayOrderDayType.REST,
            dayTypeLabel = "Rest",
            title = "Rest Day",
            reason = "Календар або recovery debt ставить паузу: сьогодні без силового блоку.",
            primaryActionLabel = "Відкрити план дня",
            outcomeText = "Recovery без штрафу",
            durationText = "Rest",
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.AI
        )
    }
}

private fun TodayTrainingDecision.toTrainingOrder(
    fallbackMainQuest: QuestUiModel?,
    reasonText: String,
    actionText: String,
    outcomeText: String,
    accent: TodayOrderAccent = TodayOrderAccent.PRIMARY
): TodayOrderUiModel =
    TodayOrderUiModel(
        dayType = TodayOrderDayType.TRAINING,
        dayTypeLabel = "Training",
        title = workoutName ?: fallbackMainQuest?.title ?: "Workout",
        reason = reasonText,
        primaryActionLabel = actionText,
        outcomeText = outcomeText,
        durationText = fallbackMainQuest.durationText("45 хв"),
        readinessProgress = readinessProgress(),
        actionEnabled = true,
        accent = accent
    )

private fun TodayTrainingDecision.readinessProgress(): Float =
    (readinessScore / 100f).coerceIn(0f, 1f)

private fun TodayTrainingDecision.noExcuseReason(): String =
    if (reason.contains("missed", ignoreCase = true)) {
        "Система зафіксувала пропуск. Наступна оптимальна дія: короткий протокол без торгу."
    } else {
        "Readiness $readinessScore% нижче плану. Система скорочує дію до мінімального переможного блоку."
    }

private fun QuestUiModel?.durationText(fallback: String): String =
    this?.estimatedDurationMinutes?.let { "$it хв" } ?: fallback

private fun QuestUiModel?.rewardText(fallback: String): String =
    this?.rewardXp?.let { "+$it XP" } ?: fallback

private fun RecoveryDebtLevel.displayLabel(): String =
    when (this) {
        RecoveryDebtLevel.LOW -> "низький"
        RecoveryDebtLevel.MODERATE -> "помірний"
        RecoveryDebtLevel.HIGH -> "високий"
        RecoveryDebtLevel.CRITICAL -> "критичний"
    }
