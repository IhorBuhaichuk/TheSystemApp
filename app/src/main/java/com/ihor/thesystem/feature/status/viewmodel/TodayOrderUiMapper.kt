package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.core.ui.toUserFacingWorkoutName
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType

internal fun TodayTrainingDecision?.toTodayOrderUiModel(
    fallbackMainQuest: QuestUiModel?
): TodayOrderUiModel {
    val decision = this ?: return TodayOrderUiModel(
        dayType = TodayOrderDayType.TRAINING,
        dayTypeLabel = "Тренування",
        title = fallbackMainQuest?.title?.toUserFacingWorkoutName()
            ?: "Готуємо план на сьогодні",
        reason = "Показник готовності ще оновлюється. Поки показано звичайний план без підвищення навантаження.",
        primaryActionLabel = "План ще готується",
        outcomeText = fallbackMainQuest.rewardText("Безпечний варіант"),
        durationText = fallbackMainQuest.durationText("План"),
        readinessProgress = 0f,
        actionEnabled = false,
        accent = TodayOrderAccent.PRIMARY
    )

    return when (decision.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> decision.toTrainingOrder(
            fallbackMainQuest = fallbackMainQuest,
            reasonText = "Готовність — ${decision.readinessScore}%. Накопичена втома — ${decision.recoveryDebt.level.displayLabel()}. Можна поступово підвищувати навантаження.",
            actionText = "Відкрити тренування",
            outcomeText = fallbackMainQuest.rewardText("Нагорода за виконання"),
            accent = TodayOrderAccent.SUCCESS
        )
        TodayTrainingDecisionType.STANDARD_TRAINING -> decision.toTrainingOrder(
            fallbackMainQuest = fallbackMainQuest,
            reasonText = "Готовність — ${decision.readinessScore}%. Перешкод для тренування немає, можна виконувати звичайний план.",
            actionText = "Відкрити тренування",
            outcomeText = fallbackMainQuest.rewardText("Нагорода за виконання")
        )
        TodayTrainingDecisionType.REDUCED_LOAD -> decision.toTrainingOrder(
            fallbackMainQuest = fallbackMainQuest,
            reasonText = "Готовність — ${decision.readinessScore}%. Накопичена втома — ${decision.recoveryDebt.level.displayLabel()}. Сьогодні працюємо легше, щоб зберегти ритм.",
            actionText = "Відкрити легкий план",
            outcomeText = "Досвід без перевтоми",
            accent = TodayOrderAccent.WARNING
        )
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> TodayOrderUiModel(
            dayType = TodayOrderDayType.RECOVERY,
            dayTypeLabel = "Відновлення",
            title = "Активне відновлення",
            reason = "Через низьку готовність або накопичену втому силове тренування сьогодні не рекомендоване.",
            primaryActionLabel = "Відкрити відновлення",
            outcomeText = "Відновлення і серія",
            durationText = fallbackMainQuest.durationText("12 хв"),
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.SUCCESS
        )
        TodayTrainingDecisionType.NO_EXCUSE -> TodayOrderUiModel(
            dayType = TodayOrderDayType.NO_EXCUSE,
            dayTypeLabel = "Короткий мінімум",
            title = "Коротке тренування",
            reason = decision.noExcuseReason(),
            primaryActionLabel = "Відкрити 7 хв",
            outcomeText = "Серія і квест",
            durationText = fallbackMainQuest.durationText("7 хв"),
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.ERROR
        )
        TodayTrainingDecisionType.DELOAD -> TodayOrderUiModel(
            dayType = TodayOrderDayType.DELOAD,
            dayTypeLabel = "Розвантаження",
            title = "Полегшене тренування",
            reason = "Накопичена втома — ${decision.recoveryDebt.level.displayLabel()}. Зменшуємо вагу й кількість підходів, щоб відновитися.",
            primaryActionLabel = "Відкрити легкий план",
            outcomeText = "Відновлення без втрати ритму",
            durationText = fallbackMainQuest.durationText("30 хв"),
            readinessProgress = decision.readinessProgress(),
            actionEnabled = true,
            accent = TodayOrderAccent.WARNING
        )
        TodayTrainingDecisionType.REST -> TodayOrderUiModel(
            dayType = TodayOrderDayType.REST,
            dayTypeLabel = "Відпочинок",
            title = "День відпочинку",
            reason = "За календарем або через втому сьогодні відпочиваємо від силового тренування.",
            primaryActionLabel = "Відкрити план дня",
            outcomeText = "Без штрафу",
            durationText = "Відпочинок",
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
        dayTypeLabel = "Тренування",
        title = (workoutName ?: fallbackMainQuest?.title)?.toUserFacingWorkoutName()
            ?: "Тренування",
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
        "Попереднє тренування пропущено. Сьогодні достатньо короткого тренування, щоб повернутися до ритму."
    } else {
        "Готовність — $readinessScore%. Система пропонує коротке тренування, яке допоможе зберегти ритм."
    }

private fun QuestUiModel?.durationText(fallback: String): String =
    this?.estimatedDurationMinutes?.let { "$it хв" } ?: fallback

private fun QuestUiModel?.rewardText(fallback: String): String =
    this?.rewardXp?.let { "+$it досвіду" } ?: fallback

private fun RecoveryDebtLevel.displayLabel(): String =
    when (this) {
        RecoveryDebtLevel.LOW -> "низька"
        RecoveryDebtLevel.MODERATE -> "помірна"
        RecoveryDebtLevel.HIGH -> "висока"
        RecoveryDebtLevel.CRITICAL -> "критична"
    }
