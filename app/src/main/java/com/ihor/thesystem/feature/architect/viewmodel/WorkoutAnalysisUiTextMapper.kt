package com.ihor.thesystem.feature.architect.viewmodel

import com.ihor.thesystem.domain.model.AnnualProgressStatus
import com.ihor.thesystem.domain.model.ExerciseProgressStatus
import com.ihor.thesystem.domain.model.MotivationLevelResult

object WorkoutAnalysisUiTextMapper {
    val motivationExplanation: String =
        "Рівень розраховується за 4 показниками:\n" +
            "• особистий прогрес від стартового результату;\n" +
            "• виконання плану річної прогресії;\n" +
            "• стабільність тренувань за останні тижні;\n" +
            "• силовий показник відносно доступного benchmark.\n\n" +
            "Якщо частини даних ще немає, система використовує нейтральне значення, щоб не занижувати оцінку."

    fun motivationBreakdown(result: MotivationLevelResult): List<Pair<String, Int>> =
        listOf(
            "Особистий прогрес" to result.componentScores.personalProgressScore,
            "План прогресії" to result.componentScores.planProgressScore,
            "Стабільність" to result.componentScores.consistencyScore,
            "Силовий benchmark" to result.componentScores.strengthBenchmarkScore
        )

    fun exerciseStatusLabel(status: ExerciseProgressStatus): String =
        when (status) {
            ExerciseProgressStatus.Improved -> "Покращення"
            ExerciseProgressStatus.Stable -> "Стабільно"
            ExerciseProgressStatus.Decreased -> "Просідання"
        }

    fun annualStatusLabel(status: AnnualProgressStatus): String =
        when (status) {
            AnnualProgressStatus.OnPlan -> "За планом"
            AnnualProgressStatus.BelowPlan -> "Нижче плану"
            AnnualProgressStatus.AbovePlan -> "Вище плану"
            AnnualProgressStatus.NoPlan -> "Готово до плану"
        }
}
